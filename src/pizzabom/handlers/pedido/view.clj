(ns pizzabom.handlers.pedido.view
  (:require
   [clojure.string :as str]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

;; ---------------------------------------------------------------------------
;; Pantalla de busqueda por telefono - En una forma :action "/pedido/buscar" es la ruta
;; ---------------------------------------------------------------------------

(defn buscar-view []
  [:div.container.mt-5
   [:div.row.justify-content-center
    [:div.col-md-7
     [:div.card.shadow-lg
      [:div.card-header.bg-primary.text-white.text-center
       [:h4.mb-0 [:i.bi.bi-telephone-fill.me-2] "Tomar Pedido"]]
      [:div.card-body.p-4
       [:div.row.g-2.mb-3
        [:div.col-md-4.d-grid
         [:button.btn.btn-primary.btn-lg
          {:type "button" :onclick "mostrarTelefonoCliente()"}
          [:i.bi.bi-house.me-2] "Cliente domicilio"]]
        [:div.col-md-4.d-grid
         [:form {:method "POST" :action "/pedido/buscar"}
          (anti-forgery-field)
          [:input {:type "hidden" :name "telefono" :value "0"}]
          [:input {:type "hidden" :name "tipo" :value "recoger"}]
          [:button.btn.btn-outline-primary.btn-lg.w-100 {:type "submit"}
           [:i.bi.bi-car-front.me-2] "Venta Local"]]]
        [:div.col-md-4.d-grid
         [:form {:method "POST" :action "/pedido/buscar"}
          (anti-forgery-field)
          [:input {:type "hidden" :name "telefono" :value "1"}]
          [:input {:type "hidden" :name "tipo" :value "Drive-Thru"}]
          [:button.btn.btn-outline-primary.btn-lg.w-100 {:type "submit"}
           [:i.bi.bi-shop.me-2] "Drive-Thru"]]]]
       [:form#telefono-cliente-form {:method "POST" :action "/pedido/buscar" :style "display:none;"}
        (anti-forgery-field)
        [:input {:type "hidden" :name "tipo" :value "domicilio"}]
        [:div.mb-4
         [:label.form-label.fw-bold {:for "telefono"} "Teléfono del cliente"]
         [:input.form-control.form-control-lg
          {:id          "telefono"
           :name        "telefono"
           :type        "tel"
           :placeholder "686-123-4567"
           :required    true}]]
        [:div.d-grid
         [:button.btn.btn-primary.btn-lg {:type "submit"}
          [:i.bi.bi-search.me-2] "Buscar"]]]
       [:script
        "function mostrarTelefonoCliente(){
           var form = document.getElementById('telefono-cliente-form');
           var tel = document.getElementById('telefono');
           if(form) form.style.display = 'block';
           if(tel) tel.focus();
         }"]]]]]])

;; ---------------------------------------------------------------------------
;; Sección del Cliente
;; ---------------------------------------------------------------------------

(defn- cliente-encontrado [cliente]
  [:div.alert.alert-success.mb-3
   [:h6.fw-bold [:i.bi.bi-person-check.me-2] (:nombre cliente)]
   [:small
    [:span.me-3 [:i.bi.bi-telephone.me-1] (:telefono cliente)]
    (when-not (str/blank? (:calle cliente))
      [:span [:i.bi.bi-geo-alt.me-1] (:calle cliente) ", " (:colonia cliente)])]
   [:input {:type "hidden" :name "cliente_id" :value (:id cliente)}]
   [:input {:type "hidden" :name "telefono"   :value (:telefono cliente)}]])

(defn- nuevo-cliente-form [telefono]
  [:div.card.border-warning.mb-3
   [:div.card-header.bg-warning.text-dark.fw-bold
    [:i.bi.bi-person-plus.me-2] "Cliente nuevo — registrar"]
   [:div.card-body
    [:div.row.g-2
     [:div.col-md-6
      [:label.form-label.fw-semibold {:for "nombre"} "Nombre *"]
      [:input.form-control {:id "nombre" :name "nombre" :type "text"
                            :placeholder "Nombre completo" :required true}]]
     [:div.col-md-6
      [:label.form-label.fw-semibold {:for "telefono"} "Teléfono *"]
      [:input.form-control {:id "telefono" :name "telefono" :type "tel"
                            :value telefono :required true}]]
     [:div.col-md-8
      [:label.form-label.fw-semibold {:for "calle"} "Calle y número"]
      [:input.form-control {:id "calle" :name "calle" :type "text"
                            :placeholder "Av. Juárez 123"}]]
     [:div.col-md-4
      [:label.form-label.fw-semibold {:for "colonia"} "Colonia"]
      [:input.form-control {:id "colonia" :name "colonia" :type "text"
                            :placeholder "Colonia"}]]
     [:div.col-12
      [:label.form-label.fw-semibold {:for "referencias"} "Referencias"]
      [:input.form-control {:id "referencias" :name "referencias" :type "text"
                            :placeholder "Frente a la farmacia, portón azul..."}]]]]])

(defn- cliente-rapido-form [telefono tipo]
  (let [nombre (if (= tipo "Drive-Thru") "Venta Local" "Drive-Thru")]
    [:div.alert.alert-info.mb-3
     [:h6.fw-bold.mb-1 [:i.bi.bi-lightning-charge.me-2] nombre]
     [:small "Pedido rápido sin datos de domicilio."]
     [:input {:type "hidden" :name "nombre" :value nombre}]
     [:input {:type "hidden" :name "telefono" :value telefono}]
     [:input {:type "hidden" :name "calle" :value ""}]
     [:input {:type "hidden" :name "colonia" :value ""}]
     [:input {:type "hidden" :name "referencias" :value ""}]]))

;; ---------------------------------------------------------------------------
;; Grid de productos agrupados por categoria — con pestañas y tarjetas
;; ---------------------------------------------------------------------------

(defn- producto-card [p]
  [:div
   {:class "col-6 col-md-4 col-lg-3 producto-card-item"
    :data-search (str/lower-case (:nombre p))}
   [:div.card.h-100.shadow-sm.text-center
    [:div.card-body.p-2.d-flex.flex-column.justify-content-between
     [:div.fw-semibold.mb-2
      {:style "font-size:0.9rem; line-height:1.3;"}
      (:nombre p)]
     [:div
      [:div.text-success.fw-bold.fs-5.mb-2
       (str "$" (format "%.0f" (double (:precio p))))]
      [:div.d-flex.justify-content-center.align-items-center.gap-1
       [:button.btn.btn-outline-secondary.btn-sm
        {:type    "button"
         :onclick (str "adjQty('qty-" (:id p) "',-1)")}
        "−"]
       [:input.form-control.form-control-sm.text-center.qty-input
        {:type        "number"
         :name        (str "qty-" (:id p))
         :value       "0"
         :min         "0"
         :max         "99"
         :style       "width:52px;"
         :data-id     (str (:id p))
         :data-nombre (:nombre p)
         :data-precio (str (:precio p))
         :onchange    "calcularTotal()"}]
       [:button.btn.btn-outline-primary.btn-sm
        {:type    "button"
         :onclick (str "adjQty('qty-" (:id p) "',1)")}
        "+"]]]]]])

(defn- productos-section [productos]
  (let [grouped   (group-by :categoria productos)
        cats      (sort (keys grouped))
        first-cat (first cats)
        n         (count cats)
        indexed   (map-indexed vector cats)]
    [:div.mb-3
     [:div.input-group.mb-3
      [:span.input-group-text [:i.bi.bi-search]]
      [:input.form-control
       {:id "producto-search"
        :type "search"
        :placeholder "Buscar producto..."
        :autocomplete "off"
        :oninput "filtrarProductos()"}]]
     ;; Botones de categoría — JS propio, sin Bootstrap tabs
     [:div.d-flex.flex-wrap.gap-2.mb-3
      (for [[i cat] indexed]
        [:button
         {:type    "button"
          :id      (str "btn-cat-" i)
          :class   (if (= cat first-cat) "btn btn-primary btn-sm" "btn btn-outline-secondary btn-sm")
          :onclick (str "showCat(" i "," n ")")}
         cat])]
     ;; Paneles de productos
     (for [[i cat] indexed]
        [:div
        {:id    (str "cat-pane-" i)
         :style (if (= cat first-cat) "display:block;" "display:none;")}
        [:div.row.g-2
         (map producto-card (get grouped cat))]
        [:div.producto-empty.alert.alert-info.my-2
         {:style "display:none;"}
         "No se encontraron productos con esa búsqueda."]])]))

;; ---------------------------------------------------------------------------
;; Forma de orden completa
;; ---------------------------------------------------------------------------

(defn orden-view [{:keys [cliente telefono tipo productos]}]
  (let [tipo          (or tipo "domicilio")
        no-productos? (empty? productos)]
    [:div.container-fluid
     [:form#pedido-form {:method "POST" :action "/pedido/guardar"}
      (anti-forgery-field)
      [:input {:type "hidden" :name "total" :id "total-hidden" :value "0"}]

      [:div.row.g-2.mb-5

       ;; ── Columna izquierda: cliente + productos ──────────────────────────
       [:div.col-lg-8

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-person.me-1] "Cliente"]
         [:div.card-body.py-2
          (if cliente
            (cliente-encontrado cliente)
            (if (= tipo "domicilio")
              (nuevo-cliente-form telefono)
              (cliente-rapido-form telefono tipo)))]]

        [:div.card.shadow-sm
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-grid.me-1] "Productos"]
         [:div.card-body.p-2
          (if no-productos?
            [:div.alert.alert-warning.m-2
             "No hay productos activos. Agréguelos en el catálogo de Productos."]
            (productos-section productos))]]]

       ;; ── Columna derecha: entrega + pago + notas ────────────────────────
       [:div.col-lg-4

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-truck.me-1] "Entrega"]
         [:div.card-body.py-2
          [:div.form-check
           [:input.form-check-input {:type "radio" :name "tipo" :id "t1"
                                     :value "domicilio" :checked (= tipo "domicilio")}]
           [:label.form-check-label {:for "t1"} [:i.bi.bi-house.me-1] "A domicilio"]]
          [:div.form-check
           [:input.form-check-input {:type "radio" :name "tipo" :id "t2"
                                     :value "recoger" :checked (= tipo "recoger")}]
           [:label.form-check-label {:for "t2"} [:i.bi.bi-shop.me-1] "Recoger en tienda"]]
          [:div.form-check
           [:input.form-check-input {:type "radio" :name "tipo" :id "t3"
                                     :value "Drive-Thru" :checked (= tipo "Drive-Thru")}]
           [:label.form-check-label {:for "t3"} [:i.bi.bi-car-front.me-1] "Drive-Thru"]]]]

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-list-check.me-1] "Pedido"]
         [:div.card-body.py-2
          [:div#pedido-lista
           [:div.text-muted.small.py-2 "Seleccione productos para armar el pedido."]]
          [:div.border-top.mt-2.pt-2.d-flex.justify-content-between.align-items-center
           [:span.fw-bold "Total"]
           [:span#pedido-lista-total.fw-bold.fs-5 "$0.00"]]]]

        [:div.card.shadow-sm.mb-2
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-cash.me-1] "Pago"]
         [:div.card-body.py-2
          [:label.form-label.fw-semibold.small {:for "paga-con"} "¿Con cuánto paga?"]
          [:div.input-group
           [:span.input-group-text "$"]
           [:input.form-control.form-control-lg
            {:id "paga-con" :type "number" :name "paga_con" :value "0" :min "0" :step "1"
             :onchange "calcularCambio()" :oninput "calcularCambio()"}]]]]

        [:div.card.shadow-sm
         [:div.card-header.bg-secondary.text-white.fw-bold.py-1
          [:i.bi.bi-chat-left-text.me-1] "Notas"]
         [:div.card-body.py-2
          [:input.form-control {:type "text" :name "notas"
                                :placeholder "Sin jalapeños, extra queso..."}]]]]]

      ;; ── Barra fija abajo: total + cambio + guardar ─────────────────────
      [:div
       {:style (str "position:fixed; bottom:0; left:0; right:0; z-index:1040;"
                    "background:#212529; color:#fff;"
                    "padding:0.5rem 1.5rem;"
                    "display:flex; align-items:center; justify-content:space-between; gap:1rem;"
                    "box-shadow:0 -2px 8px rgba(0,0,0,0.3);")}
       [:div.d-flex.gap-4.align-items-center
        [:div
         [:div {:style "font-size:0.7rem; color:#adb5bd;"} "TOTAL"]
         [:div.fw-bold.fs-4 {:id "total-display"} "$0.00"]]
        [:div
         [:div {:style "font-size:0.7rem; color:#adb5bd;"} "CAMBIO"]
         [:div.fw-bold.fs-4 {:id "cambio-display"} "$0.00"]]]
       [:div.d-flex.gap-2.align-items-center
        [:a.btn.btn-outline-light.btn-sm {:href "/pedido"}
         [:i.bi.bi-arrow-left.me-1] "Nueva búsqueda"]
        [:button.btn.btn-success.btn-lg.px-4
         {:type "submit"}
         [:i.bi.bi-check-circle.me-2] "Guardar Pedido"]]]]]))

;; ---------------------------------------------------------------------------
;; Recibo
;; ---------------------------------------------------------------------------

(defn recibo-view [pedido detalle]
  (let [cambio (:cambio pedido 0)
        tipo   (:tipo pedido)]
    [:div.container.mt-4
     [:style "@media print {
       .no-print { display:none !important; }
       nav, .navbar { display:none !important; }
       .card { border:none !important; box-shadow:none !important; }
       .card-header { background:#000 !important; color:#fff !important; -webkit-print-color-adjust:exact; print-color-adjust:exact; }
       body { margin:0 !important; padding:0 !important; }
       .container, .container-fluid { max-width:100% !important; padding:0 !important; margin:0 !important; }
       div[style*='height: 70px'] { display:none !important; }
       div[style*='margin-top:32px'] { margin-top:0 !important; max-height:none !important; overflow:visible !important; }
     }"]
     [:div.card.shadow-lg
      [:div.card-header.bg-success.text-white
       [:div.d-flex.justify-content-between.align-items-center
        [:h4.mb-0 [:i.bi.bi-receipt.me-2] "Pedido #" (:id pedido)]
        [:span.badge.bg-light.text-dark.fs-6
         (case tipo
           "domicilio" "Domicilio"
           "Drive-Thru" "Drive-Thru"
           "Recoger en tienda")]]]

      [:div.card-body
       [:div.mb-3
        [:h6.fw-bold [:i.bi.bi-person.me-2] "Cliente"]
        [:p.mb-0 (:cliente_nombre pedido)]
        (when (= tipo "domicilio")
          [:p.mb-0.text-muted
           (:calle pedido) ", Col. " (:colonia pedido)
           (when-not (str/blank? (:referencias pedido))
             [:span.d-block.fst-italic "Ref: " (:referencias pedido)])])]

       [:hr]

       [:table.table.table-sm
        [:thead [:tr [:th "Producto"] [:th.text-center "Cant"] [:th.text-end "Subtotal"]]]
        [:tbody
         (for [d detalle]
           [:tr {:key (:id d)}
            [:td (:producto_nombre d)]
            [:td.text-center (:cantidad d)]
            [:td.text-end (format "$%.2f" (double (:subtotal d)))]])]
        [:tfoot
         [:tr.fw-bold
          [:td {:colspan "2"} "TOTAL"]
          [:td.text-end (format "$%.2f" (double (:total pedido 0)))]]]]

       [:hr]

       [:div.row.text-center
        [:div.col-6
         [:div.text-muted.small "Paga con"]
         [:div.fs-4.fw-bold (format "$%.2f" (double (:paga_con pedido 0)))]]
        [:div.col-6
         [:div.text-muted.small "Cambio"]
         [:div.fs-2.fw-bold
          {:class (if (>= cambio 0) "text-success" "text-danger")}
          (format "$%.2f" (double cambio))]]]]

      [:div.card-footer.no-print.d-flex.gap-2
       [:a.btn.btn-primary {:href "/pedido"}
        [:i.bi.bi-telephone.me-1] "Nuevo Pedido"]
       [:a.btn.btn-secondary {:href "/despacho"}
        [:i.bi.bi-truck.me-1] "Ir a Despacho"]
       [:button.btn.btn-outline-dark
        {:type "button" :onclick "window.print()"}
        [:i.bi.bi-printer.me-1] "Imprimir"]]]]))

;; ---------------------------------------------------------------------------
;; JS: total + change calculador - calcular la feria del billete con lo que pago el cliente
;; ---------------------------------------------------------------------------

(defn orden-js []
  [:script
   "function showCat(idx, total) {
      for (var j = 0; j < total; j++) {
        var pane = document.getElementById('cat-pane-' + j);
        var btn  = document.getElementById('btn-cat-' + j);
        if (pane) pane.style.display = (j === idx) ? 'block' : 'none';
        if (btn)  btn.className = (j === idx) ? 'btn btn-primary btn-sm' : 'btn btn-outline-secondary btn-sm';
      }
      filtrarProductos();
    }
    function filtrarProductos() {
      var input = document.getElementById('producto-search');
      var q = input ? input.value.trim().toLowerCase() : '';

      document.querySelectorAll('[id^=\"cat-pane-\"]').forEach(function(pane){
        var visible = 0;
        pane.querySelectorAll('.producto-card-item').forEach(function(card){
          var name = card.dataset.search || '';
          var match = !q || name.indexOf(q) !== -1;
          card.style.display = match ? '' : 'none';
          if (match) visible += 1;
        });

        var empty = pane.querySelector('.producto-empty');
        if (empty) empty.style.display = visible === 0 ? 'block' : 'none';
      });
    }
    function adjQty(name, delta) {
      var el = document.querySelector('input[name=\"' + name + '\"]');
      el.value = Math.max(0, Math.min(99, (parseInt(el.value, 10) || 0) + delta));
      calcularTotal();
    }
    function money(n) {
      return '$' + (Number(n) || 0).toFixed(2);
    }
    function renderPedidoLista(total) {
      var lista = document.getElementById('pedido-lista');
      var listaTotal = document.getElementById('pedido-lista-total');
      if (!lista || !listaTotal) return;

      var items = [];
      document.querySelectorAll('.qty-input').forEach(function(el){
        var qty = parseInt(el.value, 10) || 0;
        if (qty > 0) {
          var precio = parseFloat(el.dataset.precio) || 0;
          items.push({
            name: el.name,
            nombre: el.dataset.nombre || 'Producto',
            qty: qty,
            precio: precio,
            subtotal: qty * precio
          });
        }
      });

      lista.innerHTML = '';
      if (items.length === 0) {
        var empty = document.createElement('div');
        empty.className = 'text-muted small py-2';
        empty.textContent = 'Seleccione productos para armar el pedido.';
        lista.appendChild(empty);
      } else {
        items.forEach(function(item){
          var row = document.createElement('div');
          row.className = 'd-flex align-items-center justify-content-between gap-2 py-2 border-bottom';

          var info = document.createElement('div');
          info.className = 'flex-grow-1';

          var nombre = document.createElement('div');
          nombre.className = 'fw-semibold small';
          nombre.textContent = item.nombre;

          var subtotal = document.createElement('div');
          subtotal.className = 'text-muted small';
          subtotal.textContent = item.qty + ' x ' + money(item.precio) + ' = ' + money(item.subtotal);

          info.appendChild(nombre);
          info.appendChild(subtotal);

          var controls = document.createElement('div');
          controls.className = 'btn-group btn-group-sm';

          var minus = document.createElement('button');
          minus.type = 'button';
          minus.className = 'btn btn-outline-secondary';
          minus.setAttribute('aria-label', 'Quitar ' + item.nombre);
          minus.textContent = '−';
          minus.onclick = function(){ adjQty(item.name, -1); };

          var qty = document.createElement('span');
          qty.className = 'btn btn-outline-secondary disabled';
          qty.textContent = item.qty;

          var plus = document.createElement('button');
          plus.type = 'button';
          plus.className = 'btn btn-outline-primary';
          plus.setAttribute('aria-label', 'Agregar ' + item.nombre);
          plus.textContent = '+';
          plus.onclick = function(){ adjQty(item.name, 1); };

          controls.appendChild(minus);
          controls.appendChild(qty);
          controls.appendChild(plus);
          row.appendChild(info);
          row.appendChild(controls);
          lista.appendChild(row);
        });
      }

      listaTotal.textContent = money(total);
    }
    function calcularTotal(){
      var t=0;
      document.querySelectorAll('.qty-input').forEach(function(el){
        var qty = Math.max(0, Math.min(99, parseInt(el.value,10)||0));
        el.value = qty;
        t += qty * (parseFloat(el.dataset.precio)||0);
      });
      document.getElementById('total-display').textContent=money(t);
      document.getElementById('total-hidden').value=t.toFixed(2);
      renderPedidoLista(t);
      calcularCambio();
    }
    function calcularCambio(){
      var t=parseFloat(document.getElementById('total-hidden').value)||0;
      var p=parseFloat(document.getElementById('paga-con').value)||0;
      var c=p-t;
      var el=document.getElementById('cambio-display');
      el.textContent = c>=0 ? money(c) : 'Insuficiente';
      el.className = c>=0 ? 'fw-bold fs-4 text-success' : 'fw-bold fs-4 text-danger';
    }
    document.addEventListener('DOMContentLoaded',function(){
      calcularTotal();
      document.getElementById('pedido-form').addEventListener('submit',function(e){
        var total = parseFloat(document.getElementById('total-hidden').value)||0;
        var paga = parseFloat(document.getElementById('paga-con').value)||0;
        var cambio = paga-total;

        if(total<=0){
          e.preventDefault();
          alert('Seleccione al menos un producto.');
          return;
        }
        if(paga<=0){
          e.preventDefault();
          alert('Ingrese con cuánto paga el cliente.');
          document.getElementById('paga-con').focus();
          return;
        }
        if(cambio<0){
          e.preventDefault();
          alert('El pago no alcanza para cubrir el total del pedido.');
          document.getElementById('paga-con').focus();
          return;
        }
        if(!confirm('Total: '+money(total)+'\\nPaga con: '+money(paga)+'\\nCambio: '+money(cambio)+'\\n\\n¿Está conforme con el pago?')){
          e.preventDefault();
        }
      });
    });"])
