const request = require('supertest');
const app = require('./app');

describe('Backend API Integration Tests', () => {
  it('GET / should return health status message', async () => {
    const res = await request(app).get('/');
    expect(res.statusCode).toEqual(200);
    expect(res.body).toHaveProperty('message');
    expect(res.body.message).toContain('AI Translate Keyboard Backend API is running');
  });
});
