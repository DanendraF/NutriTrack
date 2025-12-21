package config

import (
	"os"
)

type Config struct {
	Environment            string
	Port                   string
	FirebaseCredentialsPath string
	AllowedOrigins         []string
}

func Load() *Config {
	return &Config{
		Environment:            getEnv("ENVIRONMENT", "development"),
		Port:                   getEnv("PORT", "8080"),
		FirebaseCredentialsPath: getEnv("FIREBASE_CREDENTIALS_PATH", "serviceAccountKey.json"),
		AllowedOrigins: []string{
			getEnv("ALLOWED_ORIGIN", "*"),
		},
	}
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
