package main

import "C"
import "fmt"

func main() {
	// Nothing to do.
}

//export GenerateGreeting
func GenerateGreeting(name *C.char) *C.char {
	greeting := fmt.Sprintf("Hello from Golang, %v!", name)

	return C.CString(greeting)
}
