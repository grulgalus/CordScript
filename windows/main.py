import tkinter as tk
import threading, asyncio, re, discord
from discord.ext import commands

def run_bot(token, code):
    prefix = re.search(r'prefix:\s*"([^"]+)"', code)
    prefix = prefix.group(1) if prefix else "!"
    intents = discord.Intents.default()
    intents.message_content = True
    client = commands.Bot(command_prefix=prefix, intents=intents)

    for match in re.finditer(r'command\s+(\w+)\s*\{\s*reply\s+"([^"]+)"\s*\}', code):
        cmd, text = match.group(1), match.group(2)
        async def make_cmd(ctx, t=text): await ctx.send(t)
        make_cmd.__name__ = cmd
        client.add_command(commands.Command(make_cmd, name=cmd))

    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    loop.run_until_complete(client.start(token))

root = tk.Tk()
root.title("CordBot Windows")
tk.Label(root, text="Token:").pack()
entry = tk.Entry(root, width=50); entry.pack()
tk.Label(root, text="Kód:").pack()
text = tk.Text(root, height=10); text.insert("1.0", 'prefix: "!"\ncommand ping {\n reply "Pong z Windows!"\n}')
text.pack()
tk.Button(root, text="Start", command=lambda: threading.Thread(target=run_bot, args=(entry.get(), text.get("1.0", "end")), daemon=True).start()).pack()
root.mainloop()
