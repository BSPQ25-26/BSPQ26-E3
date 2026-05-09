import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from '../App';

beforeEach(() => {
  global.fetch = jest.fn();
});

afterEach(() => {
  jest.clearAllMocks();
});

describe('App routing', () => {
  it('renders Login view by default', () => {
    render(<App />);
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('navigates to Register when Create account is clicked', () => {
    render(<App />);
    fireEvent.click(screen.getByText('Create account'));
    expect(screen.getByRole('heading', { name: 'Create account' })).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
  });

  it('navigates back to Login when Back to login is clicked from Register', () => {
    render(<App />);
    fireEvent.click(screen.getByText('Create account'));
    fireEvent.click(screen.getByText('Back to login'));
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('shows success message on Login after successful registration', async () => {
    global.fetch.mockResolvedValueOnce({ ok: true });

    render(<App />);
    fireEvent.click(screen.getByText('Create account'));
    fireEvent.click(screen.getByText('Register'));

    await waitFor(() => {
      expect(screen.getByText(/Account created/)).toBeInTheDocument();
    });
    // Back on Login page after registration
    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });

  it('renders Dashboard after successful login', async () => {
    // login, then profile (404), then items (empty)
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({
          profile: { id: 'u1', username: 'alice', phone: '111' },
          email: 'alice@example.com',
        }),
      })
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => ({ items: [], total: 0 }) })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<App />);
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByText(/Welcome, alice/)).toBeInTheDocument();
    });
  });

  it('returns to Login after logout from Dashboard', async () => {
    global.fetch
      .mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: async () => ({
          profile: { id: 'u1', username: 'alice', phone: '111' },
          email: 'alice@example.com',
        }),
      })
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => ({ items: [], total: 0 }) })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<App />);
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByText('Login'));

    expect(await screen.findByText(/Welcome, alice/)).toBeInTheDocument();

    fireEvent.click(screen.getByLabelText('Show user information'));
    fireEvent.click(screen.getByText('Sign out'));

    expect(screen.getByRole('heading', { name: 'Sign in' })).toBeInTheDocument();
  });
});
