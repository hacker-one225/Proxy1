package main

import (
	"fmt"
	"io"
	"net"
	"os"
)

// Clé d'obfuscation glissante pour briser la signature réseau (DPI / IDS)
const ObfuscationKey byte = 0xA5

// Structure personnalisée pour intercepter et modifier le flux en direct
type ObfuscatedConn struct {
	net.Conn
}

// Réécriture de la fonction d'écriture pour appliquer l'obfuscation à la volée
func (o ObfuscatedConn) Write(b []byte) (n int, err error) {
	obfuscated := make([]byte, len(b))
	for i := 0; i < len(b); i++ {
		obfuscated[i] = b[i] ^ ObfuscationKey
	}
	return o.Conn.Write(obfuscated)
}

// Réécriture de la fonction de lecture pour rétablir les données d'origine
func (o ObfuscatedConn) Read(b []byte) (n int, err error) {
	n, err = o.Conn.Read(b)
	if err == nil {
		for i := 0; i < n; i++ {
			b[i] = b[i] ^ ObfuscationKey
		}
	}
	return n, err
}

func proxyBridge(source net.Conn, destination net.Conn) {
	defer source.Close()
	defer destination.Close()
	// Transfert haute performance via le tunnel d'obfuscation
	_, _ = io.Copy(source, destination)
}

func handleConnection(client net.Conn, targetAddr string) {
	// Connexion vers la machine cible
	server, err := net.Dial("tcp", targetAddr)
	if err != nil {
		fmt.Printf("[-] Impossible de joindre la cible: %v\n", err)
		client.Close()
		return
	}

	// Encapsulation des connexions standards dans notre structure furtive
	secureClient := ObfuscatedConn{Conn: client}
	secureServer := ObfuscatedConn{Conn: server}

	// Lancement des flux asynchrones modifiés
	go proxyBridge(secureClient, secureServer)
	go proxyBridge(secureServer, secureClient)
}

func main() {
	localPort := ":8080"
	targetAddr := "127.0.0.1:9000" // Modifier par l'IP de votre serveur C2 ou cible d'audit

	listener, err := net.Listen("tcp", localPort)
	if err != nil {
		fmt.Printf("[-] Erreur d'écoute sur le port %s: %v\n", localPort, err)
		os.Exit(1)
	}
	defer listener.Close()

	fmt.Printf("[+] Moteur Furtif Actif sur le port %s -> %s\n", localPort, targetAddr)

	for {
		client, err := listener.Accept()
		if err != nil {
			fmt.Printf("[-] Erreur d'acceptation: %v\n", err)
			continue
		}
		go handleConnection(client, targetAddr)
	}
}
