USE outdoor_gear_rental;

-- 统一使用本地图片路径，避免外链 CDN 防盗链导致浏览器无法加载
UPDATE gear_info SET
  main_image  = CONCAT('/images/gears/', id, '-main.jpg'),
  hover_image = CONCAT('/images/gears/', id, '-hover.jpg')
WHERE status = 1;
