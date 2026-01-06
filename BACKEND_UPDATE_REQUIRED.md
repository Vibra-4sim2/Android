# 🔧 Backend Update Required for Enhanced Authentication Flow

## ⚠️ Critical Backend Changes Needed

To support the new authentication flow, your backend **MUST** be updated to return additional fields in the Google Sign-In response.

---

## 📋 Required Changes

### 1. Update Google Sign-In Endpoint Response

**Endpoint:** `POST /auth/google`

**Current Response (❌ Old):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Required Response (✅ New):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": true
}
```

---

## 💻 Implementation Examples

### Node.js/Express Example

```javascript
// controllers/authController.js

const { OAuth2Client } = require('google-auth-library');
const jwt = require('jsonwebtoken');
const User = require('../models/User');

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

exports.googleSignIn = async (req, res) => {
  try {
    const { idToken } = req.body;
    
    // Verify Google token
    const ticket = await client.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_CLIENT_ID,
    });
    
    const payload = ticket.getPayload();
    const { email, given_name, family_name, picture } = payload;
    
    // Check if user exists
    let user = await User.findOne({ email });
    let isNewUser = false;
    
    if (!user) {
      // Create new user
      user = await User.create({
        email,
        firstName: given_name,
        lastName: family_name,
        avatar: picture,
        authProvider: 'google',
        Gender: 'Not specified', // Default value
        role: 'user'
      });
      isNewUser = true;
      
      console.log(`✅ New Google user created: ${email}`);
    } else {
      console.log(`✅ Existing Google user logged in: ${email}`);
    }
    
    // Generate JWT
    const access_token = jwt.sign(
      { userId: user._id, email: user.email },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );
    
    // ✅ IMPORTANT: Return enhanced response
    res.status(200).json({
      access_token,
      userId: user._id.toString(),
      isNewUser
    });
    
  } catch (error) {
    console.error('Google sign-in error:', error);
    res.status(400).json({
      message: 'Invalid Google token',
      error: error.message
    });
  }
};
```

### NestJS/TypeScript Example

```typescript
// auth/auth.controller.ts

import { Controller, Post, Body } from '@nestjs/common';
import { AuthService } from './auth.service';

interface GoogleSignInDto {
  idToken: string;
}

interface GoogleSignInResponse {
  access_token: string;
  userId: string;
  isNewUser: boolean;
}

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('google')
  async googleSignIn(@Body() dto: GoogleSignInDto): Promise<GoogleSignInResponse> {
    const { idToken } = dto;
    
    // Verify Google token
    const googleUser = await this.authService.verifyGoogleToken(idToken);
    
    // Check if user exists
    let user = await this.authService.findUserByEmail(googleUser.email);
    let isNewUser = false;
    
    if (!user) {
      // Create new user
      user = await this.authService.createGoogleUser({
        email: googleUser.email,
        firstName: googleUser.given_name,
        lastName: googleUser.family_name,
        avatar: googleUser.picture,
      });
      isNewUser = true;
    }
    
    // Generate JWT
    const access_token = this.authService.generateJWT(user);
    
    // ✅ Return enhanced response
    return {
      access_token,
      userId: user._id.toString(),
      isNewUser,
    };
  }
}
```

### Python/Flask Example

```python
# routes/auth.py

from flask import Blueprint, request, jsonify
from google.oauth2 import id_token
from google.auth.transport import requests
from models import User
from utils.jwt import generate_token
import os

auth_bp = Blueprint('auth', __name__)

@auth_bp.route('/auth/google', methods=['POST'])
def google_sign_in():
    try:
        data = request.get_json()
        id_token_str = data.get('idToken')
        
        # Verify Google token
        idinfo = id_token.verify_oauth2_token(
            id_token_str, 
            requests.Request(), 
            os.getenv('GOOGLE_CLIENT_ID')
        )
        
        email = idinfo['email']
        given_name = idinfo.get('given_name', '')
        family_name = idinfo.get('family_name', '')
        picture = idinfo.get('picture', '')
        
        # Check if user exists
        user = User.query.filter_by(email=email).first()
        is_new_user = False
        
        if not user:
            # Create new user
            user = User(
                email=email,
                first_name=given_name,
                last_name=family_name,
                avatar=picture,
                auth_provider='google'
            )
            db.session.add(user)
            db.session.commit()
            is_new_user = True
            print(f"✅ New Google user created: {email}")
        else:
            print(f"✅ Existing Google user logged in: {email}")
        
        # Generate JWT
        access_token = generate_token(user.id)
        
        # ✅ Return enhanced response
        return jsonify({
            'access_token': access_token,
            'userId': str(user.id),
            'isNewUser': is_new_user
        }), 200
        
    except Exception as e:
        return jsonify({
            'message': 'Invalid Google token',
            'error': str(e)
        }), 400
```

---

## 🧪 Testing Your Backend Changes

### Test with cURL (New User)

```bash
curl -X POST http://localhost:3000/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU..."
  }'
```

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": true
}
```

### Test with cURL (Existing User)

Use the same command with a user that already exists.

**Expected Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": false
}
```

---

## ✅ Verification Checklist

- [ ] Backend returns `access_token` field
- [ ] Backend returns `userId` field (as string)
- [ ] Backend returns `isNewUser` field (as boolean)
- [ ] `isNewUser` is `true` for first-time Google sign-in
- [ ] `isNewUser` is `false` for returning Google users
- [ ] Response status is 200 for successful sign-in
- [ ] Error handling returns appropriate status codes (400/401/500)

---

## 🔍 Debugging

### Check Backend Logs

Enable logging to verify the flow:

```javascript
console.log('Google Sign-In Request:', { email, isNewUser });
console.log('Response:', { access_token: token.substring(0, 20) + '...', userId, isNewUser });
```

### Monitor Android Logs

Check the Android app logs to see what's received:

```bash
adb logcat | findstr "Google Sign-In"
```

Expected logs:
```
LoginViewModel: 🔵 Starting Google Sign-In with token
AuthRepository: 🔵 Google ID Token (first 30 chars): eyJhbGciOiJSUzI1NiIsImtpZCI...
AuthRepository: ✅ Google Sign-In successful!
AuthRepository: 👤 User ID: 676d1234567890abcdef
AuthRepository: 🆕 Is new user: true
LoginViewModel: ✅ Google Sign-In successful in ViewModel
LoginScreen: 🔵 Google Sign-In detected
LoginScreen: 🆕 Is new user: true
LoginScreen: → New Google user: Navigate to PREFERENCES
```

---

## 🚨 Common Issues

### Issue 1: `isNewUser` always null in Android

**Cause:** Backend not returning the field

**Fix:** Ensure your backend response includes all three fields

### Issue 2: `userId` is undefined

**Cause:** User ID not converted to string or missing

**Fix:** 
```javascript
userId: user._id.toString()  // MongoDB
userId: user.id.toString()   // PostgreSQL
```

### Issue 3: Android still using fallback navigation

**Cause:** Response format doesn't match expected structure

**Fix:** Verify JSON field names match exactly:
- `access_token` (not `accessToken`)
- `userId` (not `user_id`)
- `isNewUser` (not `is_new_user`)

---

## 📚 Additional Resources

### Update Database Schema (if needed)

If tracking auth provider:

```javascript
// MongoDB/Mongoose Schema
const userSchema = new Schema({
  email: { type: String, required: true, unique: true },
  firstName: String,
  lastName: String,
  avatar: String,
  authProvider: { type: String, enum: ['local', 'google'], default: 'local' },
  password: { type: String }, // Optional for Google users
  // ... other fields
});
```

### JWT Payload

Ensure your JWT includes userId:

```javascript
const payload = {
  userId: user._id,
  email: user.email,
  // ... other claims
};

const token = jwt.sign(payload, SECRET, { expiresIn: '7d' });
```

---

## 🎯 Summary

**What to Update:**
1. ✅ `/auth/google` endpoint response format
2. ✅ Add `userId` field to response
3. ✅ Add `isNewUser` boolean field
4. ✅ Set `isNewUser` based on user creation vs retrieval

**Testing:**
1. Test with new Google account → `isNewUser: true`
2. Test with existing Google account → `isNewUser: false`
3. Monitor Android logs to verify correct navigation

**Timeline:**
- Backend update: ~30 minutes
- Testing: ~15 minutes
- Total: ~45 minutes

---

**Questions?** Check the main `AUTHENTICATION_FLOW_GUIDE.md` for complete flow documentation.

**Last Updated:** December 26, 2025

