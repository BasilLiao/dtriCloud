SELECT --
  ROW_NUMBER() OVER (ORDER BY CTA.TA001) AS MOCTA_ID,--
  REPLACE(CTA.TA001+'-'+CTA.TA002, ' ', '') AS TA001_TA002,--�s�O��
  CTA.TA006,--���~�~��
  CTA.TA034,--���~�~�W
  CTA.TA035,--���~�W��
  CTA.TA015,--�w�p�Ͳ���
  CTA.TA017,--�ثe�Ͳ���
  CTA.TA009,--�w�p�}�u��
  CTA.TA010,--�w�p���u��
  CTA.TA011,--���A�X1.���Ͳ�,2.�w�o��,3.�Ͳ���,Y.�w���u,y.���w���u
  CTA.TA029,--�s�O�Ƶ�(�Ȥ�/��a/�q��)
  CTA.TA054,--�s�O-�ۭq�q�Ƶ�(�۰ʱa�X)
  CTA.CREATOR,--�Ыؤu��ϥΪ�
  CTA.CREATE_DATE,--��ګإ߮ɶ�
  CTA.MODI_DATE,--��ڭק�ɶ�
  CTA.TA032,--�[�u�t(�N��)
  ISNULL(PUR.MA002,'') AS MA002,--�[�u�t(����)
  PTD.TD004,--�Ȥ�~��
  PTC.TC012,--�Ȥ�-�q��渹
  PTC.TC001+'-'+PTC.TC002 AS TC001_TC002 --���q-�q��渹
FROM --
[DTR_TW].[dbo].MOCTA AS CTA --
     LEFT JOIN [DTR_TW].[dbo].COPTD AS PTD ON (REPLACE(CTA.TA026+'-'+CTA.TA027+'-'+CTA.TA028, ' ', '') = REPLACE(PTD.TD001+'-'+PTD.TD002+'-'+PTD.TD003, ' ', ''))
     LEFT JOIN [DTR_TW].[dbo].COPTC AS PTC ON (PTC.TC001+PTC.TC002 = PTD.TD001+PTD.TD002)
     LEFT JOIN [DTR_TW].[dbo].PURMA AS PUR ON (PUR.MA001 = CTA.TA032)
WHERE --
	(CTA.TA011 = '1' OR CTA.TA011 = '2' OR CTA.TA011 = '3')
	AND (CTA.TA013 = 'Y')
	AND (CTA.TA006 LIKE '81-105%')
	AND ((CTA.TA001 = 'A511') OR (CTA.TA001= 'A521')-- �t�� �@��/���u�s�O��
	OR (CTA.TA001 = 'A512') OR (CTA.TA001= 'A522'))-- �t�~ �@��/���u�s�O��
ORDER BY --
	CTA.TA001+CTA.TA002 ASC