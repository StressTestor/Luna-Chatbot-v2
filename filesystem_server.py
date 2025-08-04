from fastmcp import FastMCP

app = FastMCP()


@app.tool()
def read_file(path: str) -> str:
    import pathlib
    return pathlib.Path(path).read_text(encoding="utf-8")


@app.tool()
def list_directory(path: str = ".") -> list[str]:
    import os
    return os.listdir(path)


@app.tool()
def get_file_info(path: str) -> dict:
    import os
    st = os.stat(path)
    return {"size": st.st_size, "mtime": st.st_mtime}


if __name__ == "__main__":
    app.run()