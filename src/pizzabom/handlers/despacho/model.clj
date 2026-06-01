(ns pizzabom.handlers.despacho.model
  (:require
   [clojure.string :as str]
   [pizzabom.models.crud :refer [db Query Update]]))

;;Aqui los estatus que son acceptados, es una variable privada, solo para usar en este namespace
(def ^:private open-statuses "('nuevo','preparando','listo','en_ruta')")

(defn get-pedidos-abiertos
  "Todas las ordenes no-cerradas con clientes y nombres de repartidores"
  []
  (let [pedidos (Query db [(str "SELECT p.*,
                                      c.nombre   AS cliente_nombre,
                                      c.telefono AS cliente_tel,
                                      c.calle, c.colonia, c.referencias,
                                      r.nombre   AS repartidor_nombre
                               FROM pedidos p
                               JOIN clientes    c ON p.cliente_id    = c.id
                               LEFT JOIN repartidores r ON p.repartidor_id = r.id
                               WHERE p.status IN " open-statuses "
                               ORDER BY p.id ASC")])
        ids     (map :id pedidos)]
    (if (seq ids)
      (let [marks       (str/join "," (repeat (count ids) "?"))
            detalles    (Query db (into [(str "SELECT pd.pedido_id, pd.cantidad, pr.nombre AS producto_nombre
                                                FROM pedido_detalle pd
                                                JOIN productos pr ON pd.producto_id = pr.id
                                                WHERE pd.pedido_id IN (" marks ")
                                                ORDER BY pd.id")]
                                         ids))
            by-pedido   (group-by :pedido_id detalles)
            detalle-items (fn [pedido-id]
                            (->> (get by-pedido pedido-id)
                                 (mapv (fn [{:keys [cantidad producto_nombre]}]
                                         (str cantidad " x " producto_nombre)))))]
        (mapv #(assoc % :detalle_items (detalle-items (:id %))) pedidos))
      pedidos)))

(defn get-repartidores
  []
  (Query db ["SELECT * FROM repartidores WHERE activo = 'T' ORDER BY nombre"]))

(defn cambiar-status!
  [pedido-id status]
  (Update db :pedidos {:status status} ["id = ?" pedido-id]))

(defn asignar!
  "Mover pedido-ids seleccionados a en_ruta y asignar el repartidor."
  [pedido-ids repartidor-id]
  (doseq [pid pedido-ids]
    (Update db :pedidos
            {:repartidor_id repartidor-id :status "en_ruta"}
            ["id = ?" pid])))
