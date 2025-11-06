/**
 * Test script để validate các chức năng authentication
 * Chạy: node tests/auth.test.js
 */

const axios = require('axios');

const BASE_URL = process.env.BASE_URL || 'http://localhost:5000/api';
const TEST_EMAIL = 'test@example.com';
const TEST_PASSWORD = 'password123';

// Test cases
const tests = [
  {
    name: 'Health Check',
    test: async () => {
      const response = await axios.get(`${BASE_URL.replace('/api', '')}/api/health`);
      console.log('✅ Health check:', response.data.message);
      return response.status === 200;
    }
  },
  {
    name: 'Register User',
    test: async () => {
      try {
        const response = await axios.post(`${BASE_URL}/auth/register`, {
          username: 'testuser',
          email: TEST_EMAIL,
          password: TEST_PASSWORD,
          firstName: 'Test',
          lastName: 'User'
        });
        
        if (response.data.success && response.data.token) {
          console.log('✅ User registration successful');
          global.testToken = response.data.token;
          return true;
        }
        return false;
      } catch (error) {
        if (error.response?.data?.message?.includes('đã tồn tại')) {
          console.log('⚠️  User already exists, trying login instead');
          return true;
        }
        throw error;
      }
    }
  },
  {
    name: 'Login User',
    test: async () => {
      const response = await axios.post(`${BASE_URL}/auth/login`, {
        usernameOrEmail: TEST_EMAIL,
        password: TEST_PASSWORD
      });
      
      if (response.data.success && response.data.token) {
        console.log('✅ User login successful');
        global.testToken = response.data.token;
        return true;
      }
      return false;
    }
  },
  {
    name: 'Get Profile',
    test: async () => {
      if (!global.testToken) {
        console.log('⚠️  No token available, skipping profile test');
        return true;
      }
      
      const response = await axios.get(`${BASE_URL}/auth/me`, {
        headers: {
          Authorization: `Bearer ${global.testToken}`
        }
      });
      
      if (response.data.success && response.data.user) {
        console.log('✅ Profile fetch successful:', response.data.user.email);
        return true;
      }
      return false;
    }
  },
  {
    name: 'Forgot Password',
    test: async () => {
      const response = await axios.post(`${BASE_URL}/auth/forgot-password`, {
        email: TEST_EMAIL
      });
      
      if (response.data.success) {
        console.log('✅ Forgot password request successful');
        return true;
      }
      return false;
    }
  },
  {
    name: 'Google Auth URLs',
    test: async () => {
      try {
        // Test if Google auth endpoint exists (will redirect)
        const response = await axios.get(`${BASE_URL}/auth/google`, {
          maxRedirects: 0,
          validateStatus: (status) => status === 302
        });
        
        if (response.status === 302 && response.headers.location?.includes('google')) {
          console.log('✅ Google auth endpoint configured correctly');
          return true;
        }
        return false;
      } catch (error) {
        if (error.response?.status === 302) {
          console.log('✅ Google auth endpoint configured correctly');
          return true;
        }
        throw error;
      }
    }
  }
];

// Run tests
async function runTests() {
  console.log('🚀 Starting authentication tests...\n');
  
  let passed = 0;
  let failed = 0;
  
  for (const testCase of tests) {
    try {
      console.log(`Running: ${testCase.name}`);
      const result = await testCase.test();
      
      if (result) {
        passed++;
      } else {
        console.log(`❌ ${testCase.name} failed`);
        failed++;
      }
    } catch (error) {
      console.log(`❌ ${testCase.name} failed:`, error.message);
      failed++;
    }
    console.log('');
  }
  
  console.log('📊 Test Results:');
  console.log(`✅ Passed: ${passed}`);
  console.log(`❌ Failed: ${failed}`);
  console.log(`📝 Total: ${tests.length}`);
  
  if (failed === 0) {
    console.log('\n🎉 All tests passed!');
  } else {
    console.log('\n⚠️  Some tests failed. Check the logs above.');
  }
}

// Check if this file is being run directly
if (require.main === module) {
  runTests().catch(console.error);
}

module.exports = { runTests };
