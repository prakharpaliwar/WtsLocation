if not exists (select * from dbo.sysobjects where id = object_id(N'[dbo].[Locations]') and OBJECTPROPERTY(id, N'IsUserTable') = 1)
CREATE TABLE [dbo].[Locations] (
	[ClientMnemonic] [varchar] (25) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[MillEnvironment] [varchar] (25) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[ClientName] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL ,
	[Default_Location] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Device_Location] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Default_Printer] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[Powerchart_Printer] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PCID] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[DocsDefaultPrinter] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ReportDefaultPrinter] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[DispFromLoc] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaRetailSR] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaRetailWS] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaLTCSR] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaLTCWS] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaHomeSR] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaHomeWS] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaMailSR] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[PhaMailWS] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[WinDefaultPrinter] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[WinBackupPrinter] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[AuthenticationMethod] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[IdleSessionTimeout] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[IdleSessionTimeoutCountdown] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[SecuredSessionTimeout] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[ManuallySecuredSessionTimeout] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[SingleActiveSession] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[SecuredSessionManualClose] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[DialogTimeout] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[SecuredSessionManualCloseAuth] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[CrmTimerEnabled] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[LoggingSeverity] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[DisconnectAllowed] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[DynamicLocationAllowed] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[BMDI_Data] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[BMDI_ODBC_Adr] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL ,
	[BMDI_ODBC_Act] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL , 
	[TWAINModel] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[AP_Image_Capture] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[AP_Image_Station] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL, 
	[Default_Tamperproof_Printer] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Default_Tamperproof_Print_Tray]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer2] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer2_Tray]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer3] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer3_Tray]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer4] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer4_Tray] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer5] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer5_Tray]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PCS_Tracking_Location]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPPRINTER] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPWS] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPInvSR1] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPInvSR2] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[SCS_Default_Login_Location] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Specimen_Label_Printer] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [PhaRetailDevice] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
    [UPDT_DT_TM] [datetime] NULL CONSTRAINT [default_date]  DEFAULT (getutcdate()), 
    [Last_Updated_By] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]
GO



IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE  CONSTRAINT_TYPE = 'PRIMARY KEY'
    AND TABLE_NAME = 'Locations' 
    AND TABLE_SCHEMA ='dbo' )
    
	ALTER TABLE [dbo].[Locations] WITH NOCHECK ADD 
		CONSTRAINT [idxClientInfo] PRIMARY KEY  CLUSTERED 
		(
			[ClientMnemonic],
			[MillEnvironment],
			[ClientName]
		)  ON [PRIMARY] 
GO


if not exists ( select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='AP_Image_Capture' )
ALTER TABLE [dbo].[Locations] ADD

	[AP_Image_Capture] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[AP_Image_Station] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL 
GO

if not exists ( select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='Default_Tamperproof_Printer' )
ALTER TABLE [dbo].[Locations] ADD 
	[Default_Tamperproof_Printer] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Default_Tamperproof_Print_Tray]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer2] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer2_Tray]  [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer3] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer3_Tray] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer4] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer4_Tray] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer5] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[Tamperproof_Printer5_Tray] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL

GO


if not exists ( select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='PCS_Tracking_Location' )
ALTER TABLE [dbo].[Locations] ADD

	[PCS_Tracking_Location] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL

GO


if not exists ( select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='PhaIPPRINTER' )
ALTER TABLE [dbo].[Locations] ADD

	[PhaIPPRINTER] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPWS] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPInvSR1] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PhaIPInvSR2] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL

GO

if not exists (select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='SCS_Default_Login_Location')

ALTER TABLE [dbo].[Locations] ADD

 [SCS_Default_Login_Location] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
 [Specimen_Label_Printer] [varchar] (50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
 
GO

if not exists (select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='PhaRetailDevice')

ALTER TABLE [dbo].[Locations] ADD

 [PhaRetailDevice] [varchar] (100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
 
GO

if not exists (select * from INFORMATION_SCHEMA.COLUMNS 
where TABLE_NAME='Locations' 
and COLUMN_NAME='UPDT_DT_TM')

ALTER TABLE [dbo].[Locations] ADD

 [UPDT_DT_TM] [datetime] NULL CONSTRAINT [default_date]  DEFAULT (getutcdate()), 
 [Last_Updated_By] [varchar] (255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
 
GO