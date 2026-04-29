import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import PlantDetailsModal from '../PlantDetailsModal';

const mockOnClose = jest.fn();
const mockPlantId = 'plant-1';
const mockUserId = 'user-1';

describe('PlantDetailsModal', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn();
  });

  it('shows loading state initially', () => {
    render(<PlantDetailsModal plantId={mockPlantId} userId={mockUserId} onClose={mockOnClose} />);
    expect(screen.getByText(/Cargando detalles/i)).toBeInTheDocument();
  });

  it('shows error if fetch fails', async () => {
    global.fetch.mockRejectedValueOnce(new Error('Failed to fetch plant details'));
    render(<PlantDetailsModal plantId={mockPlantId} userId={mockUserId} onClose={mockOnClose} />);
    await waitFor(() => {
      expect(screen.getByText(/Error loading plant details/i)).toBeInTheDocument();
    });
  });

  it('renders plant details and allows adding to cart', async () => {
    const plantDetails = {
      id: mockPlantId,
      title: 'Fern',
      amount: 10,
      description: 'A nice fern',
      quantity: 5,
      status: true,
      categoryName: 'Indoor',
      image_url: null,
    };
    global.fetch
      .mockResolvedValueOnce({ ok: true, json: async () => plantDetails }) // fetch plant details
      .mockResolvedValueOnce({ ok: true }); // add to cart

    render(<PlantDetailsModal plantId={mockPlantId} userId={mockUserId} onClose={mockOnClose} />);
    expect(await screen.findByText('Fern')).toBeInTheDocument();
    expect(screen.getByText('$10.00')).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('Quantity:'), { target: { value: '2' } });
    fireEvent.click(screen.getByText('Add to Cart'));
    await waitFor(() => {
      expect(screen.getByText(/Added to cart successfully/i)).toBeInTheDocument();
    });
  });

  it('shows error if add to cart fails', async () => {
    const plantDetails = {
      id: mockPlantId,
      title: 'Fern',
      amount: 10,
      description: 'A nice fern',
      quantity: 5,
      status: true,
      categoryName: 'Indoor',
      image_url: null,
    };
    global.fetch
      .mockResolvedValueOnce({ ok: true, json: async () => plantDetails }) // fetch plant details
      .mockRejectedValueOnce(new Error('Failed to add item to cart'));

    render(<PlantDetailsModal plantId={mockPlantId} userId={mockUserId} onClose={mockOnClose} />);
    await waitFor(() => {
      expect(screen.getByText('Fern')).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText('Add to Cart'));
    await waitFor(() => {
      expect(screen.getByText(/Error adding to cart/i)).toBeInTheDocument();
    });
  });
});
