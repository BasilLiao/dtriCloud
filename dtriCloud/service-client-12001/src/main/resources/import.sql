--system_config
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value ,sys_header)VALUES (1, 1, 'E_MAIL_PROXY', 'E_MAIL_PROXY', '',true);
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (2, 1, 'E_MAIL_PROXY', '帳號', '123MES@gmail.com');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (3, 1, 'E_MAIL_PROXY', '密碼', '123MES');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (4, 1, 'E_MAIL_PROXY', '協定', 'POST');

INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value ,sys_header)VALUES (5, 2, 'FTP_DATA_BKUP', 'FTP_DATA_BKUP', '',true);
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (6, 2, 'FTP_DATA_BKUP', 'IP', '10.1.90.10');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (7, 2, 'FTP_DATA_BKUP', 'FTP_PORT', '21');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (8, 2, 'FTP_DATA_BKUP', 'ACCOUNT', 'pm_bom_server');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (9, 2, 'FTP_DATA_BKUP', 'PASSWORD', '2fIlHs');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (10, 2, 'FTP_DATA_BKUP', 'PATH', '/PM_BOM_DBBackup/');

INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value ,sys_header)VALUES (11, 3, 'DATA_BKUP', 'DATA_BKUP', '',true);
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (12, 3, 'DATA_BKUP', 'FOLDER_NAME', '\WebAppBackupDatabase\');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (13, 3, 'DATA_BKUP', 'FILE_NAME', 'dtrimes_backup');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (14, 3, 'DATA_BKUP', 'PG_DUMP', 'C:\Program Files\PostgreSQL\10\bin\pg_dump.exe');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (15, 3, 'DATA_BKUP', 'DB_NAME', 'postgres://dbadmin:12345@localhost/dtrimes');
INSERT INTO system_config(sc_id, sc_g_id, sc_g_name, sc_name, sc_value)VALUES (16, 3, 'DATA_BKUP', 'DB_PORT', '5432');

SELECT setval('public.system_config_seq', 17, true);
DROP sequence IF EXISTS SYSTEM_CONFIG_G_SEQ CASCADE;
create sequence SYSTEM_CONFIG_G_SEQ start with 4 increment by 1;

--system_permission
--關聯用
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control,sys_status,sys_header)VALUES (1,0,0, '用無作用', '000000000000', 0000, '用無作用', '',3,true);
--系統管理
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control,sys_status,sys_header)VALUES (2,1,0, '系統管理', '000000000000', 1000, '系統管理', 'system_list',3,true);
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control,sys_status)VALUES (3,1,1, '系統管理', '111111111111', 1001, '設定-參數設定', 'system_config.basil',3);
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control,sys_status)VALUES (4,1,1, '系統管理', '000001001111', 1002, '設定-單元管理', 'system_permission.basil',3);
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control,sys_status)VALUES (5,1,1, '系統管理', '000001001111', 1005, '設定-語言欄位管理', 'system_language.basil',3);
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control)VALUES (6,1,0, '系統管理', '000001001111', 1003, '通用-群組管理', 'system_group.basil');
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control)VALUES (7,1,0, '系統管理', '000001001111', 1004, '通用-帳號管理', 'system_user.basil');
--個人功能
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control,sys_header)VALUES (8,2,0, '個人設定', '000000000000', 1100, '個人設定', 'own_list',true);
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control)VALUES (9,2,1, '個人設定', '000001001111', 1101, '設定-帳號參數', 'own_config.basil');
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control)VALUES (10,2,0, '個人設定', '000001001111', 1102, '通用-帳號資料', 'own_user.basil');
INSERT INTO system_permission(sp_id, sp_g_id, sp_type, sp_g_name, sp_permission, sys_sort, sp_name, sp_control)VALUES (11,2,0, '個人設定', '000001001111', 1103, '通用-首頁', 'index.basil');

SELECT setval('public.system_permission_seq', 11, true);
DROP sequence IF EXISTS SYSTEM_PERMISSION_G_SEQ CASCADE;
create sequence SYSTEM_PERMISSION_G_SEQ start with 3 increment by 1;

--system_group(sg_permission[特殊6(4096),特殊5(2048),特殊4(1024),特殊3(512),特殊2(256),特殊1(128),擁有(64),完全移除(32),作廢(16),新增(8),更新(4),讀取(2),訪問(1)])
----admin group
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_header,sys_status) VALUES (1,1, '系統管理者', '000000000000', 1,0000,true,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (2,1, '系統管理者', '111111111111', 2,1000,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (3,1, '系統管理者', '111111111111', 3,1001,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (4,1, '系統管理者', '111111111111', 4,1002,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (5,1, '系統管理者', '111111111111', 5,1003,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (6,1, '系統管理者', '111111111111', 6,1004,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (7,1, '系統管理者', '111111111111', 7,1005,3);

INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (8,2, '系統管理者', '111111111111', 8,1100,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (9,2, '系統管理者', '111111111111', 9,1101,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (10,2, '系統管理者', '111111111111', 10,1102,3);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_status) VALUES (11,2, '系統管理者', '111111111111', 11,1103,3);
----user group
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort,sys_header) VALUES (12,2, '一般使用者', '000000000000', 1,0000,true);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort) VALUES (13,2, '一般使用者', '000001000011', 8,1100);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort) VALUES (14,2, '一般使用者', '000001000011', 9,1101);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort) VALUES (15,2, '一般使用者', '000001000011', 10,1102);
INSERT INTO system_group(sg_id, sg_g_id, sg_name, sg_permission, sg_sp_id,sys_sort) VALUES (16,2, '一般使用者', '000001000011', 11,1103);

SELECT setval('public.system_group_seq', 15, true);
DROP sequence IF EXISTS SYSTEM_GROUP_G_SEQ CASCADE;
create sequence SYSTEM_GROUP_G_SEQ start with 3 increment by 1;

--system_user
INSERT INTO system_user(su_id,su_account, su_e_name, su_email, su_name, su_password, su_position,sys_status) VALUES (1,'admin','Admin_en', 'admin@dtr.com', 'Admin', '$2a$10$1aTotRT77Ckuw0QjmFTmJ.Ar4v03HoFsZaFlJTtYG8dWAPN2V6U3O', '超級管理者',3);
INSERT INTO system_user(su_id,su_account, su_e_name, su_email, su_name, su_password, su_position) VALUES (2,'user','User_en', 'user@dtr.com', 'User', '$2a$10$4Mm5IrG70VL8WNsIQ0IuuOzU/FXunpTEPLdq8HHpIUER76A5v6Lcq', '一般使用者');
SELECT setval('public.system_user_seq', 2, true);

--su_sg_list
----admin user+group(不須關聯 header)
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,2);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,3);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,4);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,5);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,6);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,7);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,8);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,9);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,10);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(1,11);
----user user+group(不須關聯 header)
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(2,13);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(2,14);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(2,15);
INSERT INTO su_sg_list(su_id_fk,sg_id_fk) VALUES(2,16);


--system_language
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (1,2,100,1,1, '{"zh-TW":"建立時間","zh-CN":"建立时间","en-US":"Creation time","vi-VN":"xây dựng thời gian"}', 'system_config', 'syscdate','datetime');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (2,2,100,1,9, '{"zh-TW":"建立用戶","zh-CN":"建立用户","en-US":"Creation user","vi-VN":"xây dựng thời gian"}', 'system_config', 'syscuser','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (3,2,100,1,8, '{"zh-TW":"修改時間","zh-CN":"修改时间","en-US":"Modified time","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysmdate','datetime');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (4,2,100,1,7, '{"zh-TW":"修改用戶","zh-CN":"修改用户","en-US":"Modified user","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysmuser','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (5,2,100,1,6, '{"zh-TW":"擁有時間","zh-CN":"拥有时间","en-US":"Own time","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysodate','datetime');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (6,2,100,1,5, '{"zh-TW":"擁有用戶","zh-CN":"拥有用户","en-US":"own user","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysouser','password');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (7,2,100,1,4, '{"zh-TW":"群組標記","zh-CN":"群组标记","en-US":"group tag","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysheader','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (8,2,100,1,3, '{"zh-TW":"狀態","zh-CN":"状态","en-US":"state","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysstatus','select');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (9,2,100,1,2, '{"zh-TW":"排序","zh-CN":"排序","en-US":"sort","vi-VN":"xây dựng thời gian"}', 'system_config', 'syssort','number');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (10,2,100,1,11, '{"zh-TW":"備註","zh-CN":"备注","en-US":"note","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysnote','textarea');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (11,2,100,0,0, '{"zh-TW":"主鍵","zh-CN":"主键","en-US":"key","vi-VN":"xây dựng thời gian"}', 'system_config', 'scid','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (12,2,100,1,13, '{"zh-TW":"群組鍵","zh-CN":"群组键","en-US":"group key","vi-VN":"xây dựng thời gian"}', 'system_config', 'scgid','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (13,2,200,1,14, '{"zh-TW":"設定名稱","zh-CN":"设定名称","en-US":"set name","vi-VN":"xây dựng thời gian"}', 'system_config', 'scname','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (14,2,200,1,15, '{"zh-TW":"設定群組名稱","zh-CN":"设定群组名称","en-US":"set group name","vi-VN":"xây dựng thời gian"}', 'system_config', 'scgname','time');
--INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (15,2,100,1,16, '{"zh-TW":"設定值","zh-CN":"设定值","en-US":"set value","vi-VN":"xây dựng thời gian"}', 'system_config', 'scvalue','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (16,2,100,0,17, '{"zh-TW":"修改時間起","zh-CN":"修改时间起","en-US":"Modified time S","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysmdatestart','text');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_sp_control, sl_target,sl_cm_type) VALUES (17,2,100,0,18, '{"zh-TW":"修改時間終","zh-CN":"修改时间终","en-US":"Modified time E","vi-VN":"xây dựng thời gian"}', 'system_config', 'sysmdateend','text');

--Menu翻譯
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (18,1,100,1,1, '{"zh-TW":"系統列表","zh-CN":"系统列表","en-US":"System list","vi-VN":""}','system_list');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (19,1,100,1,1, '{"zh-TW":"設定-參數設定","zh-CN":"设定-参数设定","en-US":"Parameter settings","vi-VN":""}','system_config.basil');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (20,1,100,1,1, '{"zh-TW":"設定-單元管理","zh-CN":"设定-单元管理","en-US":"Unit management","vi-VN":""}','system_permission.basil');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (21,1,100,1,1, '{"zh-TW":"設定-語言欄位管理","zh-CN":"设定-语言栏位管理","en-US":"Language management","vi-VN":""}','system_language.basil');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (22,1,100,1,1, '{"zh-TW":"設定-群組管理","zh-CN":"设定-群组管理","en-US":"Group management","vi-VN":""}','system_group.basil');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (23,1,100,1,1, '{"zh-TW":"設定-帳號管理","zh-CN":"设定-帐号管理","en-US":"Account management","vi-VN":""}','system_user.basil');

INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (24,1,100,1,1, '{"zh-TW":"個人列表","zh-CN":"个人列表","en-US":"Personal list","vi-VN":""}','own_list');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (25,1,100,1,1, '{"zh-TW":"設定-帳號參數","zh-CN":"设定-帐号参数","en-US":"Account parameter","vi-VN":""}','own_config.basil');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (26,1,100,1,1, '{"zh-TW":"通用-帳號資料","zh-CN":"通用-帐号资料","en-US":"Account Information","vi-VN":""}','own_user.basil');
INSERT INTO public.system_language_cell(sl_id, sl_class,sl_c_width,sl_c_show,sys_sort, sl_language, sl_target) VALUES (27,1,100,1,1, '{"zh-TW":"通用-首頁","zh-CN":"通用-首页","en-US":"Homepage","vi-VN":"HomePage"}','index.basil');


