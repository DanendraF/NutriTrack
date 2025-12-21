package firebase

import (
	"context"
	"log"

	firebase "firebase.google.com/go/v4"
	"google.golang.org/api/option"
)

type FirebaseApp struct {
	App *firebase.App
}

func InitializeApp(ctx context.Context, credentialsPath string) (*FirebaseApp, error) {
	opt := option.WithCredentialsFile(credentialsPath)
	app, err := firebase.NewApp(ctx, nil, opt)
	if err != nil {
		return nil, err
	}

	log.Println("✅ Firebase Admin SDK initialized successfully")

	return &FirebaseApp{App: app}, nil
}

func (f *FirebaseApp) Close() {
	log.Println("🔥 Firebase app closed")
}
