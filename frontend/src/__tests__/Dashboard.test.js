import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Dashboard from '../Dashboard';

jest.mock('../PlantDetailsModal', () => ({ onClose }) => (
  <div data-testid="plant-details-modal">
    <button onClick={onClose}>Close Modal</button>
  </div>
));
jest.mock('../Cart', () => ({ onClose }) => (
  <div data-testid="cart-sidebar">
    <button onClick={onClose}>Close Cart</button>
  </div>
));

const mockUser = { id: 'u1', username: 'alice', email: 'alice@example.com', phone: '111' };

const mockPlants = [
  { id: '1', title: 'Fern', categoryName: 'Indoor', amount: 12.99, image_url: null },
  { id: '2', title: 'Cactus', categoryName: 'Outdoor', amount: 8.50, image_url: null },
];

beforeEach(() => {
  global.fetch = jest.fn();
});

afterEach(() => {
  jest.clearAllMocks();
});

describe('Dashboard', () => {
  it('renders welcome message with username', () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    expect(screen.getByText(/Welcome, alice/)).toBeInTheDocument();
  });

  it('shows loading state while fetching plants', () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    expect(screen.getByText(/Cargando plantas/)).toBeInTheDocument();
  });

  it('renders plant cards after successful fetch', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => mockPlants });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Fern')).toBeInTheDocument();
      expect(screen.getByText('Cactus')).toBeInTheDocument();
    });
  });

  it('shows error message when items fetch fails', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: false });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    await waitFor(() => {
      expect(screen.getByText(/Error:/)).toBeInTheDocument();
    });
  });

  it('filters plants by search term', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => mockPlants });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    await waitFor(() => expect(screen.getByText('Fern')).toBeInTheDocument());

    fireEvent.change(screen.getByPlaceholderText('Search by name...'), {
      target: { value: 'fern' },
    });

    expect(screen.getByText('Fern')).toBeInTheDocument();
    expect(screen.queryByText('Cactus')).not.toBeInTheDocument();
  });

  it('filters plants by category type', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => mockPlants });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    await waitFor(() => expect(screen.getByText('Fern')).toBeInTheDocument());

    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'Indoor' } });

    expect(screen.getByText('Fern')).toBeInTheDocument();
    expect(screen.queryByText('Cactus')).not.toBeInTheDocument();
  });

  it('shows no-results message when search has no matches', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => mockPlants });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    await waitFor(() => expect(screen.getByText('Fern')).toBeInTheDocument());

    fireEvent.change(screen.getByPlaceholderText('Search by name...'), {
      target: { value: 'zzzzz' },
    });

    expect(screen.getByText(/No plants match/)).toBeInTheDocument();
  });

  it('opens PlantDetailsModal when View Details is clicked', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => mockPlants });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    await waitFor(() =>
      expect(screen.getAllByText('View Details')[0]).toBeInTheDocument()
    );

    fireEvent.click(screen.getAllByText('View Details')[0]);

    expect(screen.getByTestId('plant-details-modal')).toBeInTheDocument();
  });

  it('shows cart sidebar when cart button is clicked', async () => {
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<Dashboard user={mockUser} onLogout={jest.fn()} />);

    fireEvent.click(screen.getByLabelText('Open shopping cart'));

    expect(screen.getByTestId('cart-sidebar')).toBeInTheDocument();
  });

  it('calls onLogout when Sign out is clicked', async () => {
    const mockLogout = jest.fn();
    global.fetch
      .mockResolvedValueOnce({ ok: false })
      .mockResolvedValueOnce({ ok: true, json: async () => [] });

    render(<Dashboard user={mockUser} onLogout={mockLogout} />);

    fireEvent.click(screen.getByLabelText('Show user information'));
    fireEvent.click(screen.getByText('Sign out'));

    expect(mockLogout).toHaveBeenCalledTimes(1);
  });
});
