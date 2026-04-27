import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Register from '../Register';

const mockOnRegisterSuccess = jest.fn();
const mockShowLogin = jest.fn();

beforeEach(() => {
  jest.clearAllMocks();
  global.fetch = jest.fn();
});

describe('Register', () => {
  it('renders all four form fields', () => {
    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Phone')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
  });

  it('renders Register and Back to login buttons', () => {
    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    expect(screen.getByText('Register')).toBeInTheDocument();
    expect(screen.getByText('Back to login')).toBeInTheDocument();
  });

  it('calls showLogin when Back to login is clicked', () => {
    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    fireEvent.click(screen.getByText('Back to login'));
    expect(mockShowLogin).toHaveBeenCalledTimes(1);
  });

  it('calls onRegisterSuccess with confirmation message on success', async () => {
    global.fetch.mockResolvedValueOnce({ ok: true });

    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'bob' } });
    fireEvent.change(screen.getByPlaceholderText('Email'), { target: { value: 'bob@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Phone'), { target: { value: '123456789' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'pass123' } });
    fireEvent.click(screen.getByText('Register'));

    await waitFor(() => {
      expect(mockOnRegisterSuccess).toHaveBeenCalledWith(
        expect.stringContaining('Account created')
      );
    });
  });

  it('shows server error message on failed registration', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      text: async () => 'Username already taken',
    });

    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    fireEvent.click(screen.getByText('Register'));

    await waitFor(() => {
      expect(screen.getByText('Username already taken')).toBeInTheDocument();
    });
  });

  it('shows fallback error when server returns no body', async () => {
    global.fetch.mockResolvedValueOnce({
      ok: false,
      text: async () => '',
    });

    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    fireEvent.click(screen.getByText('Register'));

    await waitFor(() => {
      expect(screen.getByText(/Registration failed/i)).toBeInTheDocument();
    });
  });

  it('shows network error when fetch throws', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Network error'));

    render(<Register onRegisterSuccess={mockOnRegisterSuccess} showLogin={mockShowLogin} />);
    fireEvent.click(screen.getByText('Register'));

    await waitFor(() => {
      expect(screen.getByText(/could not connect/i)).toBeInTheDocument();
    });
  });
});
