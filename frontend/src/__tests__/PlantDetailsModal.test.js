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
    global.fetch.mockReturnValue(new Promise(() => {}));
    render(<PlantDetailsModal plantId={mockPlantId} userId={mockUserId} onClose={mockOnClose} />);
    expect(screen.getByText(/Loading details/i)).toBeInTheDocument();
  });

  it('shows error if fetch fails', async () => {
    global.fetch.mockRejectedValue(new Error('Failed to fetch plant details'));
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
      .mockResolvedValueOnce({ ok: true, json: async () => plantDetails })
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
      .mockResolvedValueOnce({ ok: true });

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
      .mockResolvedValueOnce({ ok: true, json: async () => plantDetails })
      .mockResolvedValueOnce({ ok: true, json: async () => [] })
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

  it('renders existing reviews and allows creating a review', async () => {
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
    const existingReviews = [
      {
        id: 1,
        itemId: mockPlantId,
        authorId: mockUserId,
        authorUsername: 'alice',
        rating: 4,
        comment: 'Healthy leaves',
        createdAt: '2026-05-18T10:00:00Z',
      },
    ];
    const createdReview = {
      id: 2,
      itemId: mockPlantId,
      authorId: mockUserId,
      authorUsername: 'alice',
      rating: 5,
      comment: 'Beautiful plant',
      createdAt: '2026-05-18T11:00:00Z',
    };

    global.fetch
      .mockResolvedValueOnce({ ok: true, json: async () => plantDetails })
      .mockResolvedValueOnce({ ok: true, json: async () => existingReviews })
      .mockResolvedValueOnce({ ok: true, json: async () => createdReview });

    render(<PlantDetailsModal plantId={mockPlantId} userId={mockUserId} onClose={mockOnClose} />);

    expect(await screen.findByText('Healthy leaves')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Comment'), { target: { value: 'Beautiful plant' } });
    fireEvent.click(screen.getByText('Submit Review'));

    await waitFor(() => {
      expect(screen.getByText(/Review submitted successfully/i)).toBeInTheDocument();
    });

    expect(screen.getByText('Beautiful plant')).toBeInTheDocument();
  });
});
