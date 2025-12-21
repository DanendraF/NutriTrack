package repository

import (
	"context"
	"strings"
	"time"

	"cloud.google.com/go/firestore"
	"github.com/google/uuid"
	"github.com/nutritrack/backend/internal/models"
	"github.com/nutritrack/backend/pkg/firebase"
	"google.golang.org/api/iterator"
)

type FoodRepository struct {
	client *firestore.Client
}

func NewFoodRepository(app *firebase.FirebaseApp) (*FoodRepository, error) {
	client, err := app.App.Firestore(context.Background())
	if err != nil {
		return nil, err
	}
	return &FoodRepository{client: client}, nil
}

// CreateFood creates a new food item
func (r *FoodRepository) CreateFood(ctx context.Context, req *models.CreateFoodRequest) (*models.Food, error) {
	food := &models.Food{
		ID:             uuid.New().String(),
		Name:           req.Name,
		NameIndonesian: req.NameIndonesian,
		Category:       req.Category,
		Nutrition:      req.Nutrition,
		ServingSize:    req.ServingSize,
		Barcode:        req.Barcode,
		ImageURL:       req.ImageURL,
		IsVerified:     true,  // system-created foods are verified
		Source:         "system",
		CreatedAt:      time.Now(),
	}

	_, err := r.client.Collection("foods").Doc(food.ID).Set(ctx, food)
	if err != nil {
		return nil, err
	}

	return food, nil
}

// GetFoodByID retrieves a food by ID
func (r *FoodRepository) GetFoodByID(ctx context.Context, foodID string) (*models.Food, error) {
	doc, err := r.client.Collection("foods").Doc(foodID).Get(ctx)
	if err != nil {
		return nil, err
	}

	var food models.Food
	if err := doc.DataTo(&food); err != nil {
		return nil, err
	}

	return &food, nil
}

// SearchFoods searches for foods by query and filters
func (r *FoodRepository) SearchFoods(ctx context.Context, req *models.FoodSearchRequest) ([]*models.Food, int, error) {
	// Build query
	var iter *firestore.DocumentIterator
	if req.Category != "" {
		iter = r.client.Collection("foods").Where("category", "==", req.Category).Documents(ctx)
	} else {
		iter = r.client.Collection("foods").Documents(ctx)
	}
	defer iter.Stop()

	var allFoods []*models.Food
	searchLower := strings.ToLower(req.Query)

	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, 0, err
		}

		var food models.Food
		if err := doc.DataTo(&food); err != nil {
			continue
		}

		// Filter by search query (case-insensitive)
		if req.Query != "" {
			nameLower := strings.ToLower(food.Name)
			nameIndoLower := strings.ToLower(food.NameIndonesian)
			if !strings.Contains(nameLower, searchLower) && !strings.Contains(nameIndoLower, searchLower) {
				continue
			}
		}

		allFoods = append(allFoods, &food)
	}

	total := len(allFoods)

	// Apply pagination
	start := req.Offset
	if start > total {
		return []*models.Food{}, total, nil
	}

	end := start + req.Limit
	if end > total {
		end = total
	}

	paginatedFoods := allFoods[start:end]

	return paginatedFoods, total, nil
}

// GetFoodByBarcode retrieves a food by barcode
func (r *FoodRepository) GetFoodByBarcode(ctx context.Context, barcode string) (*models.Food, error) {
	iter := r.client.Collection("foods").Where("barcode", "==", barcode).Limit(1).Documents(ctx)
	defer iter.Stop()

	doc, err := iter.Next()
	if err == iterator.Done {
		return nil, nil // not found
	}
	if err != nil {
		return nil, err
	}

	var food models.Food
	if err := doc.DataTo(&food); err != nil {
		return nil, err
	}

	return &food, nil
}

// UpdateFood updates a food item
func (r *FoodRepository) UpdateFood(ctx context.Context, foodID string, req *models.CreateFoodRequest) (*models.Food, error) {
	updates := []firestore.Update{
		{Path: "name", Value: req.Name},
		{Path: "nameIndonesian", Value: req.NameIndonesian},
		{Path: "category", Value: req.Category},
		{Path: "nutrition", Value: req.Nutrition},
		{Path: "servingSize", Value: req.ServingSize},
	}

	if req.Barcode != "" {
		updates = append(updates, firestore.Update{Path: "barcode", Value: req.Barcode})
	}
	if req.ImageURL != "" {
		updates = append(updates, firestore.Update{Path: "imageUrl", Value: req.ImageURL})
	}

	_, err := r.client.Collection("foods").Doc(foodID).Update(ctx, updates)
	if err != nil {
		return nil, err
	}

	return r.GetFoodByID(ctx, foodID)
}

// DeleteFood deletes a food item
func (r *FoodRepository) DeleteFood(ctx context.Context, foodID string) error {
	_, err := r.client.Collection("foods").Doc(foodID).Delete(ctx)
	return err
}

// BulkCreateFoods creates multiple foods at once (for seeding)
func (r *FoodRepository) BulkCreateFoods(ctx context.Context, foods []*models.Food) error {
	batch := r.client.Batch()

	for _, food := range foods {
		if food.ID == "" {
			food.ID = uuid.New().String()
		}
		if food.CreatedAt.IsZero() {
			food.CreatedAt = time.Now()
		}

		ref := r.client.Collection("foods").Doc(food.ID)
		batch.Set(ref, food)
	}

	_, err := batch.Commit(ctx)
	return err
}

// GetFoodsByCategory retrieves foods by category
func (r *FoodRepository) GetFoodsByCategory(ctx context.Context, category string, limit int) ([]*models.Food, error) {
	iter := r.client.Collection("foods").Where("category", "==", category).Limit(limit).Documents(ctx)
	defer iter.Stop()

	var foods []*models.Food
	for {
		doc, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return nil, err
		}

		var food models.Food
		if err := doc.DataTo(&food); err != nil {
			continue
		}
		foods = append(foods, &food)
	}

	return foods, nil
}

// CountFoods returns the total number of foods
func (r *FoodRepository) CountFoods(ctx context.Context) (int, error) {
	iter := r.client.Collection("foods").Documents(ctx)
	defer iter.Stop()

	count := 0
	for {
		_, err := iter.Next()
		if err == iterator.Done {
			break
		}
		if err != nil {
			return 0, err
		}
		count++
	}

	return count, nil
}

// Close closes the Firestore client
func (r *FoodRepository) Close() error {
	return r.client.Close()
}
