package s3

import (
	"io/ioutil"
	"sync"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
)

type s3UploaderMock struct {
	db   map[string][]byte
	lock sync.Mutex
}

func newUploaderMock() *s3UploaderMock {
	u := &s3UploaderMock{}
	u.db = make(map[string][]byte)
	return u
}

func (u *s3UploaderMock) Upload(input *s3manager.UploadInput, opts ...func(*s3manager.Uploader)) (*s3manager.UploadOutput, error) {
	content, err := ioutil.ReadAll(input.Body)
	if err != nil {
		return nil, err
	}
	u.lock.Lock()
	u.db[*input.Key] = content
	u.lock.Unlock()
	return &s3manager.UploadOutput{}, nil
}

func (u *s3UploaderMock) UploadWithContext(ctx aws.Context, input *s3manager.UploadInput, opts ...func(*s3manager.Uploader)) (*s3manager.UploadOutput, error) {
	content, err := ioutil.ReadAll(input.Body)
	if err != nil {
		return nil, err
	}
	u.lock.Lock()
	u.db[*input.Key] = content
	u.lock.Unlock()
	return &s3manager.UploadOutput{}, nil
}
