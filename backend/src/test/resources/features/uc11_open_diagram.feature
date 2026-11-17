# language: ru
Функция: UC-11 Открытие диаграммы

  Сценарий: Диаграмма из файла открывается для редактирования
    Дано файл "orders.json" с содержимым:
      """
      {
        "formatVersion": 1,
        "name": "Заказы",
        "componentSet": "uml-class",
        "schema": "uml-class",
        "blocks": [
          {
            "id": "order", "type": "class", "x": 10, "y": 20, "width": 120, "height": 60, "label": "Order",
            "ports": [{"side": "RIGHT", "offset": 0.5}],
            "style": {"borderColor": "#000000", "borderWidth": 1, "backgroundColor": "#ffffff",
                      "font": "SansSerif", "fontSize": 12, "lineType": "SOLID"}
          },
          {
            "id": "payable", "type": "interface", "x": 300, "y": 20, "width": 120, "height": 60, "label": "Payable",
            "ports": [{"side": "LEFT", "offset": 0.5}],
            "style": {"borderColor": "#000000", "borderWidth": 1, "backgroundColor": "#ffffff",
                      "font": "SansSerif", "fontSize": 12, "lineType": "DASHED"}
          }
        ],
        "connectors": [
          {
            "type": "realization",
            "source": {"blockId": "order", "side": "RIGHT", "offset": 0.5},
            "target": {"blockId": "payable", "side": "LEFT", "offset": 0.5},
            "points": [{"x": 200, "y": 50}],
            "style": {"borderColor": "#000000", "borderWidth": 1, "backgroundColor": "#ffffff",
                      "font": "SansSerif", "fontSize": 12, "lineType": "DASHED"}
          }
        ]
      }
      """
    Когда пользователь открывает файл "orders.json"
    Тогда открыта диаграмма из файла "orders.json"
    И диаграмма открыта в строгом режиме
    И на диаграмме блоков: 2
    И на диаграмме коннекторов: 1

  Сценарий: A1. Файл не открывается
    Дано открыта диаграмма с набором "flowchart" без схемы
    Когда пользователь открывает файл "missing.json"
    Тогда редактор отказывает с ошибкой 404
    И активен набор компонентов "flowchart"

  Сценарий: A2. Файл в неверном формате
    Дано открыта диаграмма с набором "flowchart" без схемы
    И файл "broken.json" с содержимым:
      """
      это не диаграмма
      """
    Когда пользователь открывает файл "broken.json"
    Тогда редактор отказывает с ошибкой 422
    И активен набор компонентов "flowchart"

  Сценарий: A2. Файл неподдерживаемой версии формата
    Дано файл "future.json" с содержимым:
      """
      {"formatVersion": 99, "componentSet": "uml-class", "blocks": [], "connectors": []}
      """
    Когда пользователь открывает файл "future.json"
    Тогда редактор отказывает с ошибкой 422
    И диаграмма не открыта
