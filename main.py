import os
import threading
import asyncio
import subprocess
import re

# Nastavení pro Kivy (grafika)
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.textinput import TextInput
from kivy.uix.label import Label
from kivy.utils import platform

import discord
from discord.ext import commands

class CordBotApp(App):
    def build(self):
        self.bot_loop = None
        self.client = None
        self.bot_thread = None

        # Udržení v pozadí pro Android
        if platform == 'android':
            from jnius import autoclass
            PythonActivity = autoclass('org.kivy.android.PythonActivity')
            Context = autoclass('android.content.Context')
            PowerManager = cast('android.os.PowerManager', PythonActivity.mActivity.getSystemService(Context.POWER_SERVICE))
            self.wake_lock = PowerManager.newWakeLock(1, 'CordBot::WakeLock')
            self.wake_lock.acquire()

        layout = BoxLayout(orientation='vertical', padding=10, spacing=10)

        layout.add_widget(Label(text="Discord Token:", size_hint=(1, 0.1)))
        self.token_input = TextInput(password=True, size_hint=(1, 0.1))
        layout.add_widget(self.token_input)

        layout.add_widget(Label(text="Kód / Cesta k souboru (.disbot, .py, .js):", size_hint=(1, 0.1)))
        self.code_input = TextInput(text='bot "MujBot" {\n  prefix: "!"\n}\n\ncommand ping {\n  reply "Pong z mobilu!"\n}', size_hint=(1, 0.5))
        layout.add_widget(self.code_input)

        self.btn_start = Button(text="SPUSTIT BOTA", background_color=(0, 1, 0, 1), size_hint=(1, 0.1))
        self.btn_start.bind(on_press=self.start_bot)
        layout.add_widget(self.btn_start)

        self.btn_stop = Button(text="ZASTAVIT", background_color=(1, 0, 0, 1), size_hint=(1, 0.1))
        self.btn_stop.bind(on_press=self.stop_bot)
        layout.add_widget(self.btn_stop)

        return layout

    def start_bot(self, instance):
        token = self.token_input.text.strip()
        input_data = self.code_input.text.strip()
        
        if not token:
            self.btn_start.text = "CHYBA: CHYBÍ TOKEN!"
            return
            
        self.btn_start.text = "BOT BĚŽÍ..."
        self.btn_start.disabled = True

        self.bot_thread = threading.Thread(target=self.run_logic, args=(token, input_data), daemon=True)
        self.bot_thread.start()

    def run_logic(self, token, input_data):
        # PROFI FUNKCE: Kontrola, zda jde o soubor
        if os.path.isfile(input_data):
            if input_data.endswith('.py'):
                subprocess.Popen(["python", input_data])
                return
            elif input_data.endswith('.js'):
                subprocess.Popen(["node", input_data])
                return
            elif input_data.endswith('.disbot'):
                with open(input_data, 'r', encoding='utf-8') as f:
                    input_data = f.read()

        # Náš CordScript parser
        prefix = "!"
        prefix_match = re.search(r'prefix:\s*"([^"]+)"', input_data)
        if prefix_match: prefix = prefix_match.group(1)

        intents = discord.Intents.default()
        intents.message_content = True
        self.client = commands.Bot(command_prefix=prefix, intents=intents)

        cmd_pattern = re.compile(r'command\s+(\w+)\s*\{\s*reply\s+"([^"]+)"\s*\}')
        for match in cmd_pattern.finditer(input_data):
            cmd_name, reply_text = match.group(1), match.group(2)
            async def make_cmd(ctx, text=reply_text): await ctx.send(text)
            make_cmd.__name__ = cmd_name
            self.client.add_command(commands.Command(make_cmd, name=cmd_name))

        self.bot_loop = asyncio.new_event_loop()
        asyncio.set_event_loop(self.bot_loop)
        self.bot_loop.run_until_complete(self.client.start(token))

    def stop_bot(self, instance):
        if self.bot_loop and self.client:
            asyncio.run_coroutine_threadsafe(self.client.close(), self.bot_loop)
            self.btn_start.disabled = False
            self.btn_start.text = "SPUSTIT BOTA"

if __name__ == '__main__':
    CordBotApp().run()
