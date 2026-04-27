import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Login from '../Login';

const mockOnLoginSuccess = jest.fn();
const mockShowRegister = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
  global.fetch = jest.fn();
});

describe('Login', () => {
  it('renders email and password inputs', () => {
    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
  });

  it('renders Login and Create account buttons', () => {
    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    expect(screen.getByText('Login')).toBeInTheDocument();
    expect(screen.getByText('Create account')).toBeInTheDocument();
  });

  it('displays successMsg prop when provided', () => {
    render(
      <Login
        onLoginSuccess={mockOnLoginSuccess}
        showRegister={mockShowRegister}
        successMsg="Account created!"
      />
    );
    expect(screen.getByText('Account created!')).toBeInTheDocument();
  });

  it('calls showRegister when Create account button is clicked', () => {
    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.click(screen.getByText('Create account'));
    expect(mockShowRegister).toHaveBeenCalledTimes(1);
  });

  it('calls onLoginSuccess with merged user data on successful login', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        profile: { id: 'user-1', username: 'alice', phone: '111' },
        email: 'alice@example.com',
      }),
    });

    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'alice@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'secret' } });
    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(mockOnLoginSuccess).toHaveBeenCalledWith({
        id: 'user-1',
        username: 'alice',
        phone: '111',
        email: 'alice@example.com',
      });
    });
  });

  it('shows error message on failed login (401)', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      status: 401,
      text: async () => 'Invalid credentials',
    });

    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });
  });

  it('shows email-not-confirmed banner on 403 response', async () => {
    global.fetch.mockResolvedValueOnce({ ok: false, status: 403 });

    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByText(/confirm your email/i)).toBeInTheDocument();
    });
  });

  it('calls showRegister when server returns 404', async () => {
    global.fetch.mockResolvedValueOnce({ ok: false, status: 404 });

    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(mockShowRegister).toHaveBeenCalledTimes(1);
    });
  });

  it('shows connection error when fetch throws', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network down'));

    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByText(/could not connect/i)).toBeInTheDocument();
    });
  });

  it('shows reset email confirmation on successful password reset', async () => {
    global.fetch.mockResolvedValueOnce({ ok: true });

    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'alice@example.com' } });
    fireEvent.click(screen.getByText('Forgot password?'));

    await waitFor(() => {
      expect(screen.getByText(/Reset email sent/i)).toBeInTheDocument();
    });
  });

  it('shows error when reset password is clicked with empty email', async () => {
    render(<Login onLoginSuccess={mockOnLoginSuccess} showRegister={mockShowRegister} />);
    fireEvent.click(screen.getByText('Forgot password?'));

    await waitFor(() => {
      expect(screen.getByText(/Enter your email/i)).toBeInTheDocument();
    });
  });
});
