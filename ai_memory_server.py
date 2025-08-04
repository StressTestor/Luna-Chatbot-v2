from fastmcp import FastMCP
from datetime import datetime
from typing import List, Dict, Any

app = FastMCP()
_memory: List[Dict[str, Any]] = []


@app.tool()
def store_conversation(text: str) -> str:
    _memory.append(
        {"type": "conversation", "text": text, "ts": datetime.utcnow().isoformat()}
    )
    return "ok"


@app.tool()
def store_fact(text: str) -> str:
    _memory.append({"type": "fact", "text": text, "ts": datetime.utcnow().isoformat()})
    return "ok"


@app.tool()
def store_preference(text: str) -> str:
    _memory.append(
        {"type": "preference", "text": text, "ts": datetime.utcnow().isoformat()}
    )
    return "ok"


@app.tool()
def store_insight(text: str) -> str:
    _memory.append(
        {"type": "insight", "text": text, "ts": datetime.utcnow().isoformat()}
    )
    return "ok"


@app.tool()
def search_memory(query: str) -> List[Dict[str, Any]]:
    q = query.lower()
    return [m for m in _memory if q in m.get("text", "").lower()]


@app.tool()
def get_context_summary() -> str:
    counts: Dict[str, int] = {}
    for item in _memory:
        counts[item["type"]] = counts.get(item["type"], 0) + 1
    total = len(_memory)
    parts = [f"total={total}"] + [f'{k}={v}' for k, v in sorted(counts.items())]
    return ", ".join(parts)


@app.tool()
def get_recent_context(n: int = 5) -> List[Dict[str, Any]]:
    return _memory[-n:]


if __name__ == "__main__":
    app.run()