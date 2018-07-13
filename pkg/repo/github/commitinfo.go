package github

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/gomods/athens/pkg/config/env"
)

const (
	fetchCommitURI string = "https://api.github.com/repos/%s/%s/commits/%s"
)

// CommitInfo represents information store in go.info
type CommitInfo struct {
	Version string
	Name    string
	Short   string
	Time    time.Time
}

func getCommitInfo(owner, repo, tag string) (CommitInfo, error) {
	commit := CommitResponse{}
	commitInfo := CommitInfo{Version: tag}

	uri := fmt.Sprintf(fetchCommitURI, owner, repo, tag)

	client := http.Client{Timeout: env.Timeout()}
	resp, err := client.Get(uri)
	if err != nil {
		return commitInfo, err
	}
	defer resp.Body.Close()

	decoder := json.NewDecoder(resp.Body)
	if err := decoder.Decode(&commit); err != nil {
		return commitInfo, err
	}

	commitInfo.Name = commit.Sha
	commitInfo.Short = commit.Sha
	if commit.Sha != "" && len(commit.Sha) > 12 {
		commitInfo.Short = commit.Sha[:12]
	}
	commitInfo.Time = commit.Commit.Author.Date

	return commitInfo, nil
}

// CommitResponse represents commit information from response message of git API
type CommitResponse struct {
	Sha    string
	Commit Commit
}

// Commit represents commit information from response message of git API
type Commit struct {
	Author Author
}

// Author represents author information from response message of git API
type Author struct {
	Date time.Time
}
