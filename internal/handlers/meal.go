package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func GetMeals(c *gin.Context) {
	userID, _ := c.Get("userID")

	// TODO: Fetch meals from Firestore
	c.JSON(http.StatusOK, gin.H{
		"user_id": userID,
		"meals":   []interface{}{},
		"message": "Get meals - to be implemented",
	})
}

func CreateMeal(c *gin.Context) {
	userID, _ := c.Get("userID")

	// TODO: Create meal in Firestore
	c.JSON(http.StatusCreated, gin.H{
		"user_id": userID,
		"message": "Create meal - to be implemented",
	})
}

func GetMeal(c *gin.Context) {
	userID, _ := c.Get("userID")
	mealID := c.Param("id")

	// TODO: Fetch meal from Firestore
	c.JSON(http.StatusOK, gin.H{
		"user_id": userID,
		"meal_id": mealID,
		"message": "Get meal - to be implemented",
	})
}

func UpdateMeal(c *gin.Context) {
	userID, _ := c.Get("userID")
	mealID := c.Param("id")

	// TODO: Update meal in Firestore
	c.JSON(http.StatusOK, gin.H{
		"user_id": userID,
		"meal_id": mealID,
		"message": "Update meal - to be implemented",
	})
}

func DeleteMeal(c *gin.Context) {
	userID, _ := c.Get("userID")
	mealID := c.Param("id")

	// TODO: Delete meal from Firestore
	c.JSON(http.StatusOK, gin.H{
		"user_id": userID,
		"meal_id": mealID,
		"message": "Delete meal - to be implemented",
	})
}
