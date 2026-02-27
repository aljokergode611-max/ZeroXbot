import os
import logging
from flask import Flask, request
from telegram import Bot, Update

BOT_TOKEN = os.environ["BOT_TOKEN"]
bot = Bot(token=BOT_TOKEN)
logging.basicConfig(level=logging.INFO)

app = Flask(__name__)

@app.route("/")
def home():
    return "Bot is running!"

@app.route("/health")
def health():
    return "OK"

@app.route(f"/{BOT_TOKEN}", methods=["POST"])
def webhook():
    data = request.get_json()
    if data and "message" in data:
        chat_id = data["message"]["chat"]["id"]
        text = data["message"].get("text", "")
        if text == "/start":
            bot.send_message(chat_id=chat_id, text="مرحباً! البوت يعمل 🟢")
        elif text.startswith("/"):
            bot.send_message(chat_id=chat_id, text="أمر غير معروف. أرسل /start")
        else:
            bot.send_message(chat_id=chat_id, text=f"لقد أرسلت: {text}")
    return "OK"

if __name__ == "__main__":
    PORT = int(os.environ.get("PORT", 8080))
    RENDER_URL = os.environ.get("RENDER_EXTERNAL_URL", "")
    webhook_url = f"{RENDER_URL}/{BOT_TOKEN}"
    bot.set_webhook(url=webhook_url)
    print(f"Webhook set: {webhook_url}")
    app.run(host="0.0.0.0", port=PORT)
