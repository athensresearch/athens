package s3

import (
	"fmt"
	"io/ioutil"
	"log"
	"sync"
	"testing"

	aws "github.com/aws/aws-sdk-go/aws"
	request "github.com/aws/aws-sdk-go/aws/request"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/gomods/athens/pkg/storage/s3/mocks"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

type TestMock struct {
	mocks.APIMock
	db   map[string][]byte
	lock sync.Mutex
}

type S3Tests struct {
	suite.Suite
	client  *TestMock
	storage *Storage
}

func Test_ActionSuite(t *testing.T) {
	clientMock := getS3Mock()
	storage, err := NewWithClient("test", clientMock)
	if err != nil {
		t.Error(err)
	}

	suite.Run(t, &S3Tests{client: clientMock, storage: storage})
}

func getS3Mock() *TestMock {
	svc := new(TestMock)
	svc.db = make(map[string][]byte)

	svc.On("PutObjectWithContext", mock.AnythingOfType("*context.timerCtx"), mock.AnythingOfType("*s3.PutObjectInput")).Return(
		func(_ aws.Context, input *s3.PutObjectInput, _ ...request.Option) *s3.PutObjectOutput {
			b, e := ioutil.ReadAll(input.Body)
			if e != nil {
				log.Fatal(e)
			}

			svc.lock.Lock()
			svc.db[*input.Key] = b
			svc.lock.Unlock()

			return nil
		}, nil)

	return svc
}

// Verify returns error if S3 state differs from expected one
func Verify(t *TestMock, value map[string][]byte) error {
	t.lock.Lock()
	defer t.lock.Unlock()

	expectedLength := len(value)
	actualLength := len(t.db)
	if len(value) != len(t.db) {
		return fmt.Errorf("Length does not match. Expected: %d. Actual: %d", expectedLength, actualLength)
	}

	for k, v := range value {
		actual, ok := t.db[k]
		if !ok {
			return fmt.Errorf("Missing element %s", k)
		}

		if !sliceEqualCheck(v, actual) {
			return fmt.Errorf("Value for key %s does not match. Expected: %v, Actual: %v", k, v, actual)
		}
	}

	return nil
}

func sliceEqualCheck(a, b []byte) bool {
	if a == nil && b == nil {
		return true
	}

	if a == nil || b == nil {
		return false
	}

	if len(a) != len(b) {
		return false
	}

	for i := range a {
		if a[i] != b[i] {
			return false
		}
	}

	return true
}
