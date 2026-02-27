import os, threading
from flask import Flask
from telegram import Update
from telegram.ext import Application, CommandHandler, MessageHandler, filters

# ─── خادم ويب صغير يبقي البوت شغّال ───
app = Flask(__name__)

@app.route("/")
def home():
    return "Bot is running!"

def run_web():
    app.run(host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))

# ─── أوامر البوت (عدّل هنا كما تريد) ───
async def start(update: Update, context):
    await update.message.reply_text("مرحباً! البوت يعمل 🟢")

async def echo(update: Update, context):
    await update.message.reply_text(update.message.text)

# ─── التشغيل ───
if __name__ == "__main__":
    threading.Thread(target=run_web, daemon=True).start()

    bot = Application.builder().token(os.environ["BOT_TOKEN"]).build()
    bot.add_handler(CommandHandler("start", start))
    bot.add_handler(MessageHandler(filters.TEXT & ~filters.COMMAND, echo))
    bot.run_polling()
