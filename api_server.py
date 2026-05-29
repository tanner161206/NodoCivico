"""
Nodo Civico - API REST Flask - Entregable 3
pip install flask flask-cors
python api_server.py
Emulador Android: http://10.0.2.2:5000
"""
from flask import Flask, request, jsonify
from flask_cors import CORS
import hashlib, secrets, time
from datetime import datetime

app = Flask(__name__)
CORS(app)

users_db={};tokens_db={};reports_db={};followups_db={}
rid_seq=[1];fid_seq=[1]

CATEGORIES=[
    {"id":1,"name":"Alumbrado","icon":"ic_lightbulb"},
    {"id":2,"name":"Aseo","icon":"ic_trash"},
    {"id":3,"name":"Seguridad","icon":"ic_shield"},
    {"id":4,"name":"Servicios publicos","icon":"ic_water"},
    {"id":5,"name":"Vias","icon":"ic_road"},
    {"id":6,"name":"Espacios comunes","icon":"ic_park"},
    {"id":7,"name":"Ruido","icon":"ic_sound"},
    {"id":8,"name":"Otro","icon":"ic_flag"},
]

def hash_pw(pw): return hashlib.sha256(pw.encode()).hexdigest()
def gen_tok(): return secrets.token_hex(32)
def get_user(req):
    a=req.headers.get("Authorization","")
    if not a.startswith("Bearer "): return None
    return users_db.get(tokens_db.get(a[7:]))
def err(m,c=400): return jsonify({"error":m}),c
def ok(d,c=200): return jsonify(d),c

@app.route("/login",methods=["POST"])
def login():
    d=request.get_json(silent=True) or {}
    e=d.get("email","").strip().lower(); pw=d.get("password","")
    if not e or not pw: return err("Campos requeridos")
    u=users_db.get(e)
    if not u or u["password"]!=hash_pw(pw): return err("Credenciales invalidas",401)
    t=gen_tok(); tokens_db[t]=e
    return ok({"token":t,"user_id":u["id"],"name":u["name"],"email":u["email"],"zone":u["zone"],"role":u["role"]})

@app.route("/register",methods=["POST"])
def register():
    d=request.get_json(silent=True) or {}
    name=d.get("name","").strip(); email=d.get("email","").strip().lower()
    pw=d.get("password",""); zone=d.get("zone","").strip()
    if not name or not email or not pw: return err("Campos requeridos")
    if len(pw)<6: return err("Contrasena min 6 chars")
    if email in users_db: return err("Email ya registrado",409)
    uid=len(users_db)+1
    users_db[email]={"id":uid,"name":name,"email":email,"password":hash_pw(pw),"zone":zone,"role":"citizen"}
    t=gen_tok(); tokens_db[t]=email
    return ok({"token":t,"user_id":uid,"name":name,"email":email,"zone":zone,"role":"citizen"},201)

@app.route("/reports",methods=["GET"])
def get_reports():
    u=get_user(request)
    if not u: return err("No autorizado",401)
    r=[x for x in reports_db.values() if x["created_by"]==u["email"]]
    r.sort(key=lambda x:x["date"],reverse=True)
    return ok(r)

@app.route("/reports/<int:rid>",methods=["GET"])
def get_report(rid):
    u=get_user(request)
    if not u: return err("No autorizado",401)
    r=reports_db.get(rid)
    if not r or r["created_by"]!=u["email"]: return err("No encontrado",404)
    return ok(r)

@app.route("/reports",methods=["POST"])
def create_report():
    u=get_user(request)
    if not u: return err("No autorizado",401)
    d=request.get_json(silent=True) or {}
    t=d.get("title","").strip(); desc=d.get("description","").strip(); loc=d.get("location","").strip()
    if not t or len(t)<5: return err("Titulo invalido")
    if not desc or len(desc)<10: return err("Descripcion invalida")
    if not loc: return err("Ubicacion requerida")
    if d.get("priority","") not in ("Baja","Media","Alta"): return err("Prioridad invalida")
    i=rid_seq[0]; rid_seq[0]+=1
    r={"id":i,"title":t,"description":desc,"category_id":d.get("category_id",1),
       "status_id":d.get("status_id",1),"priority":d.get("priority","Media"),
       "location":loc,"date":d.get("date",int(time.time()*1000)),
       "image_uri":d.get("image_uri"),"created_by":u["email"]}
    reports_db[i]=r
    return ok(r,201)

@app.route("/reports/<int:rid>",methods=["PUT"])
def update_report(rid):
    u=get_user(request)
    if not u: return err("No autorizado",401)
    r=reports_db.get(rid)
    if not r or r["created_by"]!=u["email"]: return err("No encontrado",404)
    d=request.get_json(silent=True) or {}
    if "title" in d:
        if len(d["title"].strip())<5: return err("Titulo invalido")
        r["title"]=d["title"].strip()
    if "description" in d:
        if len(d["description"].strip())<10: return err("Descripcion invalida")
        r["description"]=d["description"].strip()
    for f in ("location","category_id","status_id","priority","image_uri"):
        if f in d: r[f]=d[f]
    reports_db[rid]=r
    return ok(r)

@app.route("/reports/<int:rid>",methods=["DELETE"])
def delete_report(rid):
    u=get_user(request)
    if not u: return err("No autorizado",401)
    r=reports_db.get(rid)
    if not r or r["created_by"]!=u["email"]: return err("No encontrado",404)
    del reports_db[rid]
    return ok({"success":True,"message":"Reporte eliminado"})

@app.route("/reports/<int:rid>/followups",methods=["GET"])
def get_followups(rid):
    u=get_user(request)
    if not u: return err("No autorizado",401)
    r=[f for f in followups_db.values() if f["report_id"]==rid]
    r.sort(key=lambda f:f["created_at"])
    return ok(r)

@app.route("/reports/<int:rid>/followups",methods=["POST"])
def add_followup(rid):
    u=get_user(request)
    if not u: return err("No autorizado",401)
    d=request.get_json(silent=True) or {}
    c=d.get("comment","").strip()
    if not c: return err("Comentario requerido")
    i=fid_seq[0]; fid_seq[0]+=1
    fu={"id":i,"report_id":rid,"comment":c,"created_by":u["email"],"created_at":int(time.time()*1000)}
    followups_db[i]=fu
    return ok(fu,201)

@app.route("/categories",methods=["GET"])
def get_categories():
    return ok(CATEGORIES)

@app.route("/",methods=["GET"])
def health():
    return ok({"status":"ok","app":"Nodo Civico API v3","timestamp":datetime.utcnow().isoformat()})

def seed():
    email="test@nodocivico.com"
    users_db[email]={"id":1,"name":"Usuario Test","email":email,"password":hash_pw("test1234"),"zone":"Barrio Bolivar","role":"citizen"}
    tokens_db["test_token_e3"]=email
    for title,desc,cat,status,prio,loc in [
        ("Luminaria danada en la calle 12","La luminaria lleva 3 dias apagada causando inseguridad.",1,1,"Alta","Calle 12"),
        ("Basura acumulada en el parque","Varios dias sin recoger junto al quiosco.",2,2,"Media","Parque Central"),
        ("Fuga de agua frente al bloque B","Fuga visible desde las 6 AM.",4,3,"Alta","Bloque B"),
    ]:
        i=rid_seq[0]; rid_seq[0]+=1
        reports_db[i]={"id":i,"title":title,"description":desc,"category_id":cat,"status_id":status,"priority":prio,"location":loc,"date":int(time.time()*1000),"image_uri":None,"created_by":email}
    print(f"\nNodo Civico API v3 lista")
    print(f"Usuario: {email} / test1234 | Token: test_token_e3\n")

if __name__=="__main__":
    seed()
    app.run(debug=True,host="0.0.0.0",port=5000)
