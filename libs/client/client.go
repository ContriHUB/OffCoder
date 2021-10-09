package main

/**
 * Reference: https://github.com/xalanq/cf-tool
 */

import "C"

import (
	"fmt"
	"io/ioutil"
	"net/http"
	"net/http/cookiejar"
	"net/url"
	"strings"
)

var client *http.Client = nil
var jar *cookiejar.Jar = nil

//export InitClient
func InitClient() {
	fmt.Println("GO: InitClient: Called")
	jar, _ := cookiejar.New(nil)
	client = &http.Client{Jar: jar, Transport: &http.Transport{Proxy: http.ProxyFromEnvironment}}
}

//export ReqGet
func ReqGet(URL string) *C.char {
	fmt.Println("GO: ReqGet: Called")
	data, err := GetBody(&URL)
	if err != nil {
		return C.CString("")
	}
	return C.CString(string(data))
}

//export ReqPost
func ReqPost(URL string, urlParams string) *C.char {
	fmt.Println("GO: ReqPost: Called")
	data, err := PostBody(URL, GetUrlValues(urlParams))
	if err != nil {
		return C.CString("")
	}
	return C.CString(string(data))
}

func GetUrlValues(urlParams string) url.Values {
	values := url.Values{}
	fmt.Println("GO: GetUrlValues: Called")
	if len(urlParams) > 0 {
		index := strings.Index(urlParams, "?")
		urlParams = urlParams[index+1:]

		args := strings.Split(urlParams, "&")
		for _, elem := range args {
			pair := strings.Split(elem, "=")
			val, err := url.QueryUnescape(pair[1])
			if err != nil {
				return values
			}
			values.Add(pair[0], val)
		}
	}
	return values
}

func GetBody(URL *string) ([]byte, error) {
	fmt.Println("GO: GetBody: Called")
	resp, err := client.Get(*URL)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	return ioutil.ReadAll(resp.Body)
}

func PostBody(URL string, data url.Values) ([]byte, error) {
	fmt.Println("GO: PostBody: Called")
	resp, err := client.PostForm(URL, data)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	return ioutil.ReadAll(resp.Body)
}

func main() {}
