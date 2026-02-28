#!/usr/bin/env python3
"""
ZeroxTimerBot - Auto-reply with activation code (Chat ID)
Runs on Render using Webhook + Flask
"""

import os
import logging
import requests
from flask import Flask, request as flask_request

BOT_TOKEN = os.environ["BOT_TOKEN"]
API_URL = f"https://api.telegram.org/bot{BOT_TOKEN}"
logging.basicConfig(level=logging.INFO)

app = Flask(__name__)


def send_message(chat_id, text, parse_mode="HTML"):
    """Send a message to a chat"""
    url = f"{API_URL}/sendMessage"
    data = {
        "chat_id": chat_id,
        "text": text,
        "parse_mode": parse_mode
    }
    try:
        r = requests.post(url, json=data, timeout=15)
        result = r.json()
        logging.info(f"Sent to {chat_id}: {result.get('ok', False)}")
        return result
    except Exception as e:
        logging.error(f"Error sending to {chat_id}: {e}")
        return None


def handle_message(message):
    """Handle incoming message"""
    chat_id = message["chat"]["id"]
    text = message.get("text", "")
    first_name = message.get("from", {}).get("first_name", "")
    username = message.get("from", {}).get("username", "")

    logging.info(f"Message from {first_name} (@{username}): {text}")

    if text.startswith("/start"):
        reply = (
            f"✅ <b>مرحباً {first_name}!</b>\n\n"
            f"🔑 <b>رمز التفعيل الخاص بك:</b>\n\n"
            f"<code>{chat_id}</code>\n\n"
            f"📋 اضغط على الرمز لنسخه\n"
            f"📱 ثم الصقه في إعدادات التطبيق\n\n"
            f"━━━━━━━━━━━━━━━━━━━\n"
            f"🤖 <b>ZeroX Timer Bot</b>\n"
            f"⏰ إشعارات التحضير التلقائي"
        )
        send_message(chat_id, reply)

    elif text == "/id":
        reply = f"🔑 رمز التفعيل: <code>{chat_id}</code>"
        send_message(chat_id, reply)

    elif text == "/help":
        reply = (
            "📖 <b>المساعدة</b>\n\n"
            "/start - الحصول على رمز التفعيل\n"
            "/id - عرض رمز التفعيل\n"
            "/help - المساعدة\n\n"
            "💡 الصق رمز التفعيل في إعدادات التطبيق لتصلك إشعارات التحضير التلقائي"
        )
        send_message(chat_id, reply)

    else:
        reply = (
            f"👋 أهلاً {first_name}!\n\n"
            f"🔑 رمز التفعيل: <code>{chat_id}</code>\n\n"
            f"اكتب /help للمساعدة"
        )
        send_message(chat_id, reply)


# ─── Flask Routes ───

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
        handle_message(data["message"])

    return "OK"


# ─── Main ───

if __name__ == "__main__":
    PORT = int(os.environ.get("PORT", 8080))
    RENDER_URL = os.environ.get("RENDER_EXTERNAL_URL", "")

    # تسجيل الـ Webhook
    webhook_url = f"{RENDER_URL}/{BOT_TOKEN}"
    resp = requests.post(f"{API_URL}/setWebhook", json={"url": webhook_url})
    logging.info(f"Webhook set: {webhook_url} -> {resp.json()}")

    print("🤖 ZeroxTimerBot started on Render...")
    app.run(host="0.0.0.0", port=PORT)
