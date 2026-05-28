package com.example.database

import kotlinx.coroutines.flow.Flow

data class ReplTemplate(
    val id: String,
    val name: String,
    val category: String, // "Frontend", "Backend", "System", "Creative"
    val icon: String,
    val description: String,
    val lang: String,
    val defaultFiles: Map<String, String>
)

class ReplRepository(private val replDao: ReplDao) {

    val allProjects: Flow<List<ProjectEntity>> = replDao.getAllProjects()

    fun getFilesFlow(projectId: Int): Flow<List<FileEntity>> = replDao.getFilesByProjectFlow(projectId)

    suspend fun getFiles(projectId: Int): List<FileEntity> = replDao.getFilesByProject(projectId)

    fun getCommitsFlow(projectId: Int): Flow<List<CommitEntity>> = replDao.getCommitsByProject(projectId)

    fun getDeploymentsFlow(projectId: Int): Flow<List<DeploymentEntity>> = replDao.getDeploymentsByProject(projectId)

    suspend fun getProjectById(projectId: Int): ProjectEntity? = replDao.getProjectById(projectId)

    suspend fun createProjectFromTemplate(name: String, templateId: String): Int {
        val template = templates.find { it.id == templateId } ?: templates.first()
        val project = ProjectEntity(
            name = name.ifBlank { "My ${template.name}" },
            languageType = template.lang,
            templateIcon = template.icon
        )
        val projectId = replDao.insertProject(project).toInt()

        // Insert template files
        template.defaultFiles.forEach { (path, content) ->
            replDao.insertFile(
                FileEntity(
                    projectId = projectId,
                    path = path,
                    content = content
                )
            )
        }

        // Add an initial commit
        replDao.insertCommit(
            CommitEntity(
                projectId = projectId,
                commitMessage = "Initial commit: cloned from template [${template.name}]",
                author = "ReplDeveloper"
            )
        )

        // Preload an idle deployment
        replDao.insertDeployment(
            DeploymentEntity(
                projectId = projectId,
                domainUrl = "https://${name.lowercase().replace(" ", "-")}.devrepl.app",
                status = "IDLE",
                logs = "Deployment setup complete. Waiting for first commit push."
            )
        )

        return projectId
    }

    suspend fun saveFile(projectId: Int, path: String, content: String) {
        val files = replDao.getFilesByProject(projectId)
        val existing = files.find { it.path == path }
        if (existing != null) {
            replDao.updateFile(existing.copy(content = content))
        } else {
            replDao.insertFile(FileEntity(projectId = projectId, path = path, content = content))
        }
        
        // Touch project updated timestamp
        val project = replDao.getProjectById(projectId)
        if (project != null) {
            replDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun createNewFile(projectId: Int, path: String) {
        replDao.insertFile(
            FileEntity(
                projectId = projectId,
                path = path,
                content = "// New $path file created\n"
            )
        )
    }

    suspend fun deleteFile(projectId: Int, path: String) {
        replDao.deleteFileByPath(projectId, path)
    }

    suspend fun updateProject(project: ProjectEntity) {
        replDao.updateProject(project)
    }

    suspend fun commitChanges(projectId: Int, message: String, author: String) {
        replDao.insertCommit(
            CommitEntity(
                projectId = projectId,
                commitMessage = message,
                author = author
            )
        )
    }

    suspend fun recordDeployment(projectId: Int, url: String, status: String, logsJson: String) {
        replDao.insertDeployment(
            DeploymentEntity(
                projectId = projectId,
                domainUrl = url,
                status = status,
                logs = logsJson
            )
        )
    }

    suspend fun deleteProject(projectId: Int) {
        replDao.deleteProject(projectId)
        // cascade deletes are not enabled but let's query and delete manually
        val files = replDao.getFilesByProject(projectId)
        files.forEach { replDao.deleteFile(it.id) }
    }

    companion object {
        // EXACTLY 56 PRELOADED WEB AND CREATIVE CODE TEMPLATES
        val templates = listOf(
            // --- Group 1: Web Frontend (15 templates) ---
            ReplTemplate(
                "html5", "HTML5 Canvas Sandbox", "Frontend", "🌐", "Modern responsive HTML5 canvas template with JavaScript physics loops.", "HTML",
                mapOf(
                    "index.html" to """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DevRepl Canvas Physics</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div id="container">
        <h1>DevRepl Live Canvas App</h1>
        <canvas id="physics-canvas"></canvas>
        <p>Tap anywhere to spawn colorful particles with gravity simulations!</p>
    </div>
    <script src="script.js"></script>
</body>
</html>""",
                    "style.css" to """body {
    margin: 0;
    padding: 20px;
    background: #0f141c;
    color: #f8f8f2;
    font-family: sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 90vh;
}
#container {
    text-align: center;
    max-width: 600px;
}
canvas {
    background: #181f29;
    border-radius: 12px;
    border: 2px solid #3b4252;
    box-shadow: 0 10px 30px rgba(0,0,0,0.5);
    margin-top: 15px;
}""",
                    "script.js" to """console.log("DevRepl Canvas Starter booting up...");
const canvas = document.getElementById("physics-canvas");
const ctx = canvas.getContext("2d");
canvas.width = 400;
canvas.height = 300;

let particles = [];
class Particle {
    constructor(x, y) {
        this.x = x;
        this.y = y;
        this.size = Math.random() * 8 + 4;
        this.speedX = (Math.random() - 0.5) * 6;
        this.speedY = (Math.random() - 0.5) * 6;
        this.color = `hsl(${Math.random() * 360}, 100%, 60%)`;
    }
    update() {
        this.x += this.speedX;
        this.y += this.speedY;
        this.speedY += 0.1; // gravity
        if (this.size > 0.2) this.size -= 0.1;
    }
    draw() {
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
    }
}

canvas.addEventListener("click", (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    for (let i = 0; i < 15; i++) {
        particles.push(new Particle(x, y));
    }
    console.log("Spawned 15 particles. Total active: " + particles.length);
});

function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    // Draw grid
    ctx.strokeStyle = "rgba(40, 50, 70, 0.3)";
    for(let i=0; i<canvas.width; i+=40) {
        ctx.beginPath(); ctx.moveTo(i, 0); ctx.lineTo(i, canvas.height); ctx.stroke();
    }
    for(let i=0; i<canvas.height; i+=40) {
        ctx.beginPath(); ctx.moveTo(0, i); ctx.lineTo(canvas.width, i); ctx.stroke();
    }

    particles.forEach((p, idx) => {
        p.update();
        p.draw();
        if (p.size <= 0.2) particles.splice(idx, 1);
    });
    requestAnimationFrame(animate);
}
animate();"""
                )
            ),
            ReplTemplate(
                "react_core", "React Live Grid", "Frontend", "⚛️", "React application rendered instantly with stateful layout variables.", "JSX",
                mapOf(
                    "App.jsx" to """import React, { useState } from 'react';
export default function App() {
    const [cols, setCols] = useState(4);
    return (
        <div style={{ padding: 20, color: '#e5e9f0', background: '#2e3440', borderRadius: 12 }}>
            <h2>Interactive Dynamic React Grid</h2>
            <div style={{ margin: '10px 0' }}>
                <label>Columns count: {cols} </label>
                <input type="range" min="2" max="10" value={cols} onChange={e => setCols(parseInt(e.target.value))} />
            </div>
            <div style={{
                display: 'grid',
                gridTemplateColumns: `repeat(${'$'}{cols}, 1fr)`,
                gap: 10,
                marginTop: 15
            }}>
                {Array.from({ length: 12 }).map((_, i) => (
                    <div key={i} style={{
                        background: '#4c566a',
                        padding: 15,
                        textAlign: 'center',
                        borderRadius: 6,
                        border: '1px solid #88c0d0'
                    }}>
                        Box #{i + 1}
                    </div>
                ))}
            </div>
        </div>
    );
}"""
                )
            ),
            ReplTemplate("vue_app", "Vue Stateful Counter", "Frontend", "💚", "Stateful Vue 3 template running responsive layouts.", "HTML", mapOf("index.html" to "<!-- Vue 3 template implementation -->\n<div id='app'>Vue Sandbox</div>")),
            ReplTemplate("angular_mini", "Angular Lite Playground", "Frontend", "🅰️", "A mini single-file template to run declarative Angular controller setups.", "HTML", mapOf("index.html" to "<app-root></app-root>\n<script>console.log('Angular boot successful');</script>")),
            ReplTemplate("tailwind_show", "Tailwind CSS Showcase", "Frontend", "🎨", "Modern marketing headers using Utility-First Tailwind classes.", "HTML", mapOf("index.html" to "<div class='p-12 background-slate-900 border-2 rounded-xl text-center shadow-lg'><h1 class='text-sky-400 font-extrabold text-2xl'>Seamless Tailwind</h1></div>")),
            ReplTemplate("svelte_count", "Svelte Dynamic Binding", "Frontend", "🧡", "Svelte declarative template with reactive variables and sliders.", "HTML", mapOf("App.svelte" to "<script>\nlet value = 1;\n</script>\n<main><h1>{value}</h1></main>")),
            ReplTemplate("bulma_css", "Bulma Hero Landing", "Frontend", "🇧", "Elegant Bulma CSS sections with responsive grids.", "HTML", mapOf("index.html" to "<section class='hero is-primary'><div class='hero-body'><p class='title'>Creative Hub</p></div></section>")),
            ReplTemplate("bootstrap5", "Bootstrap 5 Dashboard", "Frontend", "🗄️", "M3 layout with statistics panels styled with Bootstrap cards.", "HTML", mapOf("index.html" to "<div class='container py-4'><div class='row'><div class='col'><div class='card bg-dark text-white'><h5 class='card-header'>Cloud Storage</h5></div></div></div></div>")),
            ReplTemplate("canvas_game", "Canvas Isometric Game", "Frontend", "🕹️", "Isometric level creator using JS canvas draw loops.", "HTML", mapOf("game.js" to "console.log('Loading 2.5D Isometric Engine...');")),
            ReplTemplate("synth_mod", "Modular Audio Synthesizer", "Frontend", "🎹", "Sine-wave oscillator engine with interactive synth keys.", "HTML", mapOf("keys.js" to "console.log('Initializing WebAudio synth context...');")),
            ReplTemplate("webgl_particles", "WebGL Particle System", "Frontend", "🌌", "Raw GPU rendering simulation drawing millions of coordinates.", "HTML", mapOf("shader.glsl" to "// Vertex & fragment shaders")),
            ReplTemplate("svg_morph", "SVG Morphing Engine", "Frontend", "📐", "Calculates custom vector SVG paths to morph interactive points.", "HTML", mapOf("paths.svg" to "<svg viewBox='0 0 100 100'><path d='M10 10 L90 10 L90 90 Z'/></svg>")),
            ReplTemplate("creative_typo", "Brutalist Variable Typography", "Frontend", "✍️", "Uses CSS keyframes for experimental display lettering.", "HTML", mapOf("typography.css" to "@keyframes weightChange { 0% { font-weight: 100; } 100% { font-weight: 900; } }")),
            ReplTemplate("sticky_notes", "Sticky Notes LocalDatabase", "Frontend", "📌", "Sticky notes that persist immediately across sessions.", "HTML", mapOf("index.html" to "<div id='sticky-container'></div>")),
            ReplTemplate("speech_recognizer", "Speech-to-Text Recognizer", "Frontend", "🎙️", "Instant browser speech recognition engine for hands-free coding.", "HTML", mapOf("voice.js" to "const recognition = new webkitSpeechRecognition();")),

            // --- Group 2: Backend Services (12 templates) ---
            ReplTemplate(
                "node_express", "Node.js Express Server", "Backend", "🟢", "Full lightweight node server script simulating microservice routes.", "JS",
                mapOf(
                    "server.js" to """const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.get('/', (req, res) => {
    res.json({
        id: "repl-rest-service",
        status: "RUNNING",
        timestamp: Date.now(),
        author: "DevRepl Cloud Backend",
        endpoints: ["/api/v1/auth", "/api/v1/projects", "/api/v1/health"]
    });
});

app.get('/api/v1/health', (req, res) => {
    res.send({ status: "green", network: "active", latency: "12ms" });
});

app.listen(PORT, () => {
    console.log("Express dev server connected on port " + PORT);
    console.log("Mock Deployment active on https://express-rest-cloud.devrepl.app");
});"""
                )
            ),
            ReplTemplate(
                "python_api", "Python FastAPI Rest", "Backend", "🐍", "Asynchronous Python HTTP API endpoints with Swagger schema docs.", "Python",
                mapOf(
                    "main.py" to """from fastapi import FastAPI
import time

app = FastAPI(title="DevRepl Python REST Engine")

@app.get("/")
def read_root():
    return {
        "status": "online",
        "runtime": "Python 3.10",
        "server_time": time.time(),
        "framework": "FastAPI"
    }

@app.get("/items/{item_id}")
def read_item(item_id: int, q: str = None):
    print(f"Log: Processing queries for ID {item_id}")
    return {"item_id": item_id, "query": q, "processed": True}
"""
                )
            ),
            ReplTemplate("flask_app", "Flask Micro Framework", "Backend", "🧪", "Lighter Python microserver using web layouts.", "Python", mapOf("app.py" to "from flask import Flask\napp = Flask(__name__)\n\n@app.route('/')\ndef hello(): return 'Flask online!'")),
            ReplTemplate("ruby_sinatra", "Ruby Sinatra Controller", "Backend", "💎", "Fast web routing using Ruby's lightweight DSL.", "Ruby", mapOf("main.rb" to "require 'sinatra'\nget '/' do\n  'Sinatra Active!'\nend")),
            ReplTemplate("go_fiber", "Go Fiber HighSpeed API", "Backend", "🐹", "High-throughput server routes built using Go and standard maps.", "Go", mapOf("main.go" to "package main\nimport 'fmt'\nfunc main() { fmt.Println('Fiber active!') }")),
            ReplTemplate("bun_router", "Bun Fast Node Service", "Backend", "🍔", "Runs TypeScript routing engines natively at ultra low latency.", "TS", mapOf("index.ts" to "Bun.serve({\n  fetch(req) { return new Response('Bun server active!'); }\n});")),
            ReplTemplate("php_api", "PHP Stateless Route Engine", "Backend", "🐘", "Classic backend script handling incoming GET requests.", "PHP", mapOf("index.php" to "<?php echo json_encode(['status' => 'PHP Active']); ?>")),
            ReplTemplate("django_mini", "Django ORM Setup", "Backend", "🎯", "Mock models and SQLite query routing utilizing Django standard.", "Python", mapOf("views.py" to "from django.http import JsonResponse\ndef api(ref): return JsonResponse({'django': 'running'})")),
            ReplTemplate("kotlin_spring", "Kotlin Spring Mock Controller", "Backend", "☕", "Declarative REST API schemas in clean Spring Boot structure.", "Kotlin", mapOf("Api.kt" to "@RestController\nclass DevController {\n  @GetMapping('/')\n  fun home() = 'Spring online'\n}")),
            ReplTemplate("ktor_server", "Kotlin Ktor Microservice", "Backend", "🌊", "Lightweight asynchronous server engine using coroutines.", "Kotlin", mapOf("Server.kt" to "import io.ktor.server.engine.*\nimport io.ktor.server.netty.*\nfun main() { embeddedServer(Netty, port = 8080) {}.start(wait = true) }")),
            ReplTemplate("rust_actix", "Rust Actix Web Server", "Backend", "🦀", "Safe compilation, low memory server threads with Actix handlers.", "Rust", mapOf("main.rs" to "use actix_web::{get, App, HttpServer, Responder};")),
            ReplTemplate("graphql_service", "GraphQL Schema Server", "Backend", "☊", "Queries precise custom schemas without over-fetching API points.", "JS", mapOf("schema.js" to "const typeDefs = `type Query { hello: String }`;")),

            // --- Group 3: Languages & Algorithms (15 templates) ---
            ReplTemplate(
                "cpp_algo", "C++ Fibonacci Recurse", "System", "⚙️", "C++ algorithms compiling recursive trees with complexity graphs.", "C++",
                mapOf(
                    "main.cpp" to """#include <iostream>
#include <chrono>

using namespace std;

long long fibonacci(int n) {
    if (n <= 1) return n;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

int main() {
    int n = 35;
    cout << "C++ Fibonacci Compiler running..." << endl;
    cout << "Calculating fib(" << n << ") recursively..." << endl;
    
    auto start = chrono::high_resolution_clock::now();
    long long ans = fibonacci(n);
    auto end = chrono::high_resolution_clock::now();
    
    chrono::duration<double, milli> duration = end - start;
    cout << "Result: " << ans << endl;
    cout << "Executed in " << duration.count() << " milliseconds." << endl;
    return 0;
}"""
                )
            ),
            ReplTemplate(
                "python_turtle", "Python Turtle Art Generator", "System", "🖌️", "Calculates recursive geometric coordinates to draft vector art.", "Python",
                mapOf(
                    "turtle_render.py" to """def generate_fractal(depth, length):
    if depth == 0:
        return [length]
    # generate fractal tree coordinates
    print(f"Drawing fractal tree branch, level {depth}, thickness {length}")
    left = generate_fractal(depth - 1, length * 0.7)
    right = generate_fractal(depth - 1, length * 0.7)
    return left + right

print("DevRepl Graphics engine: Turtle Art Simulator loaded.")
branches = generate_fractal(5, 100)
print(f"Rendered fractal containing {len(branches)} unique vector segments!")"""
                )
            ),
            ReplTemplate("kotlin_sort", "Kotlin Sorting Benchmarks", "System", "🗂️", "Kotlin sorting benchmark comparing Quicksort & Heapsort.", "Kotlin", mapOf("Main.kt" to "fun main() {\n  println('Kotlin algorithm starter online')\n}")),
            ReplTemplate("rust_wasm", "Rust WebAssembly Calculator", "System", "🦀", "Compiles high-performance rust algorithms to runs client-side.", "Rust", mapOf("lib.rs" to "pub fn add(a: i32, b: i32) -> i32 { a + b }")),
            ReplTemplate("java_binary_tree", "Java Balanced Binary Search Tree", "System", "☕", "Balanced tree structures with preorder/inorder DFS traversal.", "Java", mapOf("Tree.java" to "class Node {\n  int val; Node left, right;\n}")),
            ReplTemplate("go_concurrency", "Go Multi-channel Concurrency", "System", "🐹", "Orchestrates channels and select blocks to prevent race conditions.", "Go", mapOf("channels.go" to "func main() {\n  ch := make(chan string)\n  go func() { ch <- 'data' }()\n}")),
            ReplTemplate("ts_patterns", "TypeScript Behavioral Patterns", "System", "🔒", "Type-safe Singleton, Strategy, and Observer design patterns.", "TS", mapOf("patterns.ts" to "export class Singleton {\n  private static instance: Singleton;\n}")),
            ReplTemplate("ruby_structures", "Ruby Hash Maps Profiler", "System", "💎", "Profiles ruby hash insert lookups at extreme scales.", "Ruby", mapOf("benchmark.rb" to "require 'benchmark'")),
            ReplTemplate("swift_console", "Swift Async Await App", "System", "🕊️", "Console tasks tracking async responses on concurrent task threads.", "Swift", mapOf("main.swift" to "Task { print('Swift async complete!') }")),
            ReplTemplate("haskell_fib", "Haskell Lazily Evaluated Lists", "System", "λ", "Haskell infinite lists recursively evaluating primes.", "Haskell", mapOf("main.hs" to "primes = filterPrimes [2..]\nmain = print (take 10 primes)")),
            ReplTemplate("scala_recursion", "Scala Tailrec Iterators", "System", "⚡", "Tailrecursive helper loops avoiding StackOverflow errors in Scala.", "Scala", mapOf("State.scala" to "import scala.annotation.tailrec")),
            ReplTemplate("perl_parser", "Perl Regex Parser Engine", "System", "🐪", "Regular expressions indexing string tokens from code files.", "Perl", mapOf("parser.pl" to "while(<>) { print if /pattern/; }")),
            ReplTemplate("bash_ops", "Bash Automation Builder", "System", "🐚", "CLI scripts creating nested workspace trees in directories.", "Bash", mapOf("build.sh" to "#!/bin/bash\necho 'Scaffolding projects...'")),
            ReplTemplate("assembly_emu", "8086 Assembly Emulator", "System", "💾", "Simulates low-level registers, stack frames, and interrupts.", "ASM", mapOf("program.asm" to "MOV AX, 4C00h\nINT 21h")),
            ReplTemplate("c_matrix", "C Matrix Multiplications", "System", "💻", "Optimized linear math multiplying complex vectors in memory.", "C", mapOf("matrix.c" to "int main() { printf('Optimized C runner ready'); }")),

            // --- Group 4: Data & Creative Fields (14 templates) ---
            ReplTemplate(
                "markdown_edit", "Markdown Live Previewer", "Creative", "📝", "Markdown documents which render rich styled HTML outlines.", "MD",
                mapOf(
                    "README.md" to """# DevRepl Workspace
Welcome to your offline **Markdown Preview** project!

## Features
- Fully compatible with standard CommonMark specifications.
- Instant responsive side-by-side layout rendering.
- Code blocks match custom developer syntax themes instantly!

### Getting Started
Modify this code inside the editor and watch the visual output sync instantly.
"""
                )
            ),
            ReplTemplate(
                "chess_engine", "Custom Board Chess Simulator", "Creative", "👑", "Chess engine simulating classic chess move rules.", "JS",
                mapOf(
                    "chess.js" to """class ChessGame {
    constructor() {
        this.board = this.initBoard();
        this.turn = 'white';
        console.log("Chess Game Engine initialized.");
    }
    initBoard() {
        return [
            ['r','n','b','q','k','b','n','r'],
            ['p','p','p','p','p','p','p','p'],
            ['.','.','.','.','.','.','.','.'],
            ['.','.','.','.','.','.','.','.'],
            ['.','.','.','.','.','.','.','.'],
            ['.','.','.','.','.','.','.','.'],
            ['P','P','P','P','P','P','P','P'],
            ['R','N','B','Q','K','B','N','R']
        ];
    }
    move(from, to) {
        console.log(`Move processed from ${'$'}{from} to ${'$'}{to}`);
        return true;
    }
}
const testGame = new ChessGame();
testGame.move('e2', 'e4');"""
                )
            ),
            ReplTemplate("d3_graph", "Dynamic Node D3 Visualizer", "Creative", "📊", "Generates interactive node grids based on coordinates.", "JS", mapOf("graph.js" to "console.log('Rendering interactive coordinate chart...');")),
            ReplTemplate("json_validator", "JSON Strict Validator App", "Creative", "🔍", "Validates strict JSON strings, highlighting syntax line columns.", "JS", mapOf("validator.js" to "function validate(str) { return JSON.parse(str); }")),
            ReplTemplate("csv_toolkit", "CSV Spreadsheet Exporter", "Creative", "🗃️", "Parses raw table rows to export sheets files.", "JS", mapOf("csv.js" to "console.log('Exporter helper online');")),
            ReplTemplate("svg_art_gen", "SVG Math Pattern Art", "Creative", "🎨", "Generates beautiful procedural vector spirals with angles.", "HTML", mapOf("index.html" to "<svg viewBox='0 0 200 200' id='spiral'></svg>")),
            ReplTemplate("jupyter_lite", "Notebook Interpreter Grid", "Creative", "📓", "Runs linear python blocks, printing returns under code blocks.", "Python", mapOf("notebook.ipynb" to "{\n 'cells': []\n}")),
            ReplTemplate("crypto_track", "Crypto Price Ticker", "Creative", "🪙", "Fetches crypto stats, plotting mock price charts.", "JS", mapOf("chart.js" to "console.log('Rendering candlestick timelines...');")),
            ReplTemplate("music_sequencer", "Music Beat MIDI Sequencer", "Creative", "🥁", "Drags sound pads to orchestrate retro drum rhythms.", "JS", mapOf("sequencer.js" to "const pads = Array(16).fill(0);")),
            ReplTemplate("pixel_art_editor", "Pixel Art Grid Editor", "Creative", "🖼️", "Clicks pixel matrices to color drawing canvas boards.", "HTML", mapOf("grid.js" to "console.log('Pixel grid editor size: 32x32');")),
            ReplTemplate("web_paint", "Touch Draw Paint Brush", "Creative", "🖌️", "Full canvas layout allowing finger brushes with dynamic thickness.", "HTML", mapOf("draw.js" to "console.log('Paint canvas brush tracking active');")),
            ReplTemplate("shaders_toy", "WebGL Shader Playground", "Creative", "🎆", "GPU math rendering glowing neon cosmic waves.", "HTML", mapOf("fragment.frag" to "void main() { gl_FragColor = vec4(1.0, 0.5, 0.2, 1.0); }")),
            ReplTemplate("portfolio_show", "Developer Creative Portfolio", "Creative", "💼", "A modern responsive designer resume layout.", "HTML", mapOf("index.html" to "<h1>Creative Developer Portfolio</h1>")),
            ReplTemplate("pdf_invoice", "PDF Invoice Layout Creator", "Creative", "📄", "Templates drafting print-friendly invoice slips.", "HTML", mapOf("invoice.js" to "console.log('Generating print layout specifications');"))
        )
    }
}
