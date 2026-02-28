import os
import logging
import requests
from flask import Flask, request as flask_request

BOT_TOKEN = os.environ["BOT_TOKEN"]
API_URL = f"https://api.telegram.org/bot{BOT_TOKEN}"
logging.basicConfig(level=logging.INFO)

app = Flask(__name__)


def send_message(chat_id, text):
    """إرسال رسالة عبر Telegram API مباشرة"""
    url = f"{API_URL}/sendMessage"
    payload = {"chat_id": chat_id, "text": text}
    resp = requests.post(url, json=payload)
    logging.info(f"sendMessage response: {resp.status_code}")
    return resp


@app.route("/")
def home():
    return "Bot is running!"


@app.route("/health")
def health():
    return "OK"


@app.route(f"/{BOT_TOKEN}", methods=["POST"])
def webhook():
    data = flask_request.get_json()
    logging.info(f"Received update: {data}")

    if data and "message" in data:
        chat_id = data["message"]["chat"]["id"]
        text = data["message"].get("text", "")

        if text == "/start":
            send_message(chat_id, "مرحباً! البوت يعمل 🟢")
        elif text.startswith("/"):
            send_message(chat_id, "أمر غير معروف. أرسل /start")
        else:
            send_message(chat_id, f"لقد أرسلت: {text}")

    return "OK"


if __name__ == "__main__":
    PORT = int(os.environ.get("PORT", 8080))
    RENDER_URL = os.environ.get("RENDER_EXTERNAL_URL", "")

    # تسجيل الـ Webhook
    webhook_url = f"{RENDER_URL}/{BOT_TOKEN}"
    resp = requests.post(f"{API_URL}/setWebhook", json={"url": webhook_url})
    logging.info(f"Webhook set: {webhook_url} -> {resp.json()}")

    app.run(host="0.0.0.0", port=PORT)
