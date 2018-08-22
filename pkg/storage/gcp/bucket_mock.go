package gcp

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"strings"
	"sync"
)

type bucketMock struct {
	db             map[string][]byte
	lock           sync.RWMutex
	readLockCount  int
	writeLockCount int
}

func newBucketMock() *bucketMock {
	b := &bucketMock{}
	b.db = make(map[string][]byte)
	return b
}

type bucketReader struct {
	io.Reader
	*bucketMock
}

type bucketWriter struct {
	*bucketMock
	path string
}

func (r *bucketReader) Close() error {
	r.bucketMock.readLockCount--
	r.bucketMock.lock.RUnlock()
	return nil
}

func (r *bucketWriter) Close() error {
	r.bucketMock.writeLockCount--
	r.bucketMock.lock.Unlock()
	return nil
}

func (r *bucketWriter) Write(p []byte) (int, error) {
	r.bucketMock.db[r.path] = append(r.bucketMock.db[r.path], p...)
	return len(p), nil
}

func (m *bucketMock) Delete(ctx context.Context, path string) error {
	m.lock.Lock()
	defer m.lock.Unlock()
	delete(m.db, path)
	return nil
}

func (m *bucketMock) Open(ctx context.Context, path string) (io.ReadCloser, error) {
	m.lock.RLock()
	data, ok := m.db[path]
	if !ok {
		m.lock.RUnlock()
		return nil, fmt.Errorf("path %s not found", path)
	}
	m.readLockCount++
	r := bytes.NewReader(data)
	return &bucketReader{r, m}, nil
}

func (m *bucketMock) Write(ctx context.Context, path string) io.WriteCloser {
	m.lock.Lock()
	m.db[path] = make([]byte, 0)
	m.writeLockCount++
	return &bucketWriter{m, path}
}

func (m *bucketMock) List(ctx context.Context, prefix string) ([]string, error) {
	res := make([]string, 0)

	m.lock.RLock()
	defer m.lock.RUnlock()
	for k := range m.db {
		if strings.HasPrefix(k, prefix) {
			res = append(res, k)
		}
	}
	return res, nil
}

func (m *bucketMock) Exists(ctx context.Context, path string) (bool, error) {
	m.lock.RLock()
	defer m.lock.RUnlock()
	_, found := m.db[path]
	return found, nil
}

func (m *bucketMock) ReadClosed() bool {
	return (m.readLockCount == 0)
}

func (m *bucketMock) WriteClosed() bool {
	return (m.writeLockCount == 0)
}
