import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Cart from '../Cart';

const mockOnClose = jest.fn();
const mockUserId = 'user-1';

describe('Cart', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  it('shows loading state initially', () => {
    render(<Cart userId={mockUserId} onClose={mockOnClose} />);
    expect(screen.getByText(/Loading cart/i)).toBeInTheDocument();
  });

  it('shows error if fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Failed to fetch cart'));
    render(<Cart userId={mockUserId} onClose={mockOnClose} />);
    await waitFor(() => {
      expect(screen.getByText(/Error: Failed to fetch cart/i)).toBeInTheDocument();
    });
  });

  it('shows empty cart message if no items', async () => {
    global.fetch.mockResolvedValueOnce({ ok: true, json: async () => ({ items: [], total: 0 }) });
    render(<Cart userId={mockUserId} onClose={mockOnClose} />);
    await waitFor(() => {
      expect(screen.getByText(/Your cart is empty/i)).toBeInTheDocument();
    });
  });

  it('renders cart items and allows removing', async () => {
    const cartData = {
      items: [
        { itemId: '1', title: 'Fern', amount: 10, quantity: 2 },
        { itemId: '2', title: 'Cactus', amount: 5, quantity: 1 },
      ],
      total: 25,
    };
    global.fetch
      .mockResolvedValueOnce({ ok: true, json: async () => cartData }) // fetch cart
      .mockResolvedValueOnce({ ok: true, json: async () => ({ ...cartData, items: [cartData.items[1]], total: 5 }) }); // remove item

    render(<Cart userId={mockUserId} onClose={mockOnClose} />);
    expect(await screen.findByText('Fern')).toBeInTheDocument();
    expect(screen.getByText('Cactus')).toBeInTheDocument();

    fireEvent.click(screen.getAllByLabelText('Remove item')[0]);
    expect(screen.queryByText('Fern')).not.toBeInTheDocument();
    expect(await screen.findByText('Cactus')).toBeInTheDocument();
  });

  it('handles checkout', async () => {
    const cartData = {
      items: [
        { itemId: '1', title: 'Fern', amount: 10, quantity: 2 },
      ],
      total: 20,
    };
    global.fetch
      .mockResolvedValueOnce({ ok: true, json: async () => cartData }) // fetch cart
      .mockResolvedValueOnce({ ok: true, json: async () => ({ items: [], total: 0 }) }); // checkout
    window.alert = jest.fn();
    render(<Cart userId={mockUserId} onClose={mockOnClose} />);
    expect(await screen.findByText('Fern')).toBeInTheDocument();
    fireEvent.click(screen.getByText(/Proceed to Checkout/i));
    await waitFor(() => {
      expect(window.alert).toHaveBeenCalledWith('Checkout successful!');
    });
    expect(mockOnClose).toHaveBeenCalled();
  });
});
