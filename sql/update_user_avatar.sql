-- 为没有头像的用户设置默认头像
UPDATE user 
SET userAvatar = CONCAT('https://api.dicebear.com/7.x/avataaars/svg?seed=', userAccount)
WHERE userAvatar IS NULL OR userAvatar = '';
