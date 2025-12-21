package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/nutritrack/backend/internal/models"
	"github.com/nutritrack/backend/internal/repository"
)

type FoodHandler struct {
	foodRepo *repository.FoodRepository
}

func NewFoodHandler(foodRepo *repository.FoodRepository) *FoodHandler {
	return &FoodHandler{foodRepo: foodRepo}
}

// SearchFoods searches for foods with filters
func (h *FoodHandler) SearchFoods(c *gin.Context) {
	var req models.FoodSearchRequest
	if err := c.ShouldBindQuery(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// Default values
	if req.Limit == 0 {
		req.Limit = 20
	}

	foods, total, err := h.foodRepo.SearchFoods(c.Request.Context(), &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to search foods"})
		return
	}

	// Convert to responses
	var foodResponses []*models.FoodResponse
	for _, food := range foods {
		foodResponses = append(foodResponses, food.ToResponse())
	}

	response := models.FoodSearchResponse{
		Foods:   foodResponses,
		Total:   total,
		Limit:   req.Limit,
		Offset:  req.Offset,
		HasMore: req.Offset+len(foods) < total,
	}

	c.JSON(http.StatusOK, response)
}

// GetFood retrieves a single food by ID
func (h *FoodHandler) GetFood(c *gin.Context) {
	foodID := c.Param("id")

	food, err := h.foodRepo.GetFoodByID(c.Request.Context(), foodID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Food not found"})
		return
	}

	c.JSON(http.StatusOK, food.ToResponse())
}

// GetFoodByBarcode retrieves a food by barcode
func (h *FoodHandler) GetFoodByBarcode(c *gin.Context) {
	barcode := c.Param("barcode")

	food, err := h.foodRepo.GetFoodByBarcode(c.Request.Context(), barcode)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to lookup barcode"})
		return
	}

	if food == nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Food not found for this barcode"})
		return
	}

	c.JSON(http.StatusOK, food.ToResponse())
}

// CreateFood creates a new food item (admin/system only)
func (h *FoodHandler) CreateFood(c *gin.Context) {
	var req models.CreateFoodRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	food, err := h.foodRepo.CreateFood(c.Request.Context(), &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create food"})
		return
	}

	c.JSON(http.StatusCreated, food.ToResponse())
}

// UpdateFood updates a food item (admin only)
func (h *FoodHandler) UpdateFood(c *gin.Context) {
	foodID := c.Param("id")

	var req models.CreateFoodRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	food, err := h.foodRepo.UpdateFood(c.Request.Context(), foodID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to update food"})
		return
	}

	c.JSON(http.StatusOK, food.ToResponse())
}

// DeleteFood deletes a food item (admin only)
func (h *FoodHandler) DeleteFood(c *gin.Context) {
	foodID := c.Param("id")

	err := h.foodRepo.DeleteFood(c.Request.Context(), foodID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to delete food"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "Food deleted successfully"})
}

// GetFoodsByCategory retrieves foods by category
func (h *FoodHandler) GetFoodsByCategory(c *gin.Context) {
	category := c.Param("category")
	limit := 50 // default limit

	foods, err := h.foodRepo.GetFoodsByCategory(c.Request.Context(), category, limit)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get foods"})
		return
	}

	var foodResponses []*models.FoodResponse
	for _, food := range foods {
		foodResponses = append(foodResponses, food.ToResponse())
	}

	c.JSON(http.StatusOK, gin.H{
		"category": category,
		"foods":    foodResponses,
		"total":    len(foodResponses),
	})
}
