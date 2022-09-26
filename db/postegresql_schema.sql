CREATE SEQUENCE seq_allocatedobjectid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE allocatedobject (
    allocatedobjectid bigint DEFAULT nextval('seq_allocatedobjectid'::regclass) NOT NULL,
    name character varying(255) NOT NULL
);

CREATE SEQUENCE seq_allocationauditid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE allocationaudit (
    allocationauditid bigint DEFAULT nextval('seq_allocationauditid'::regclass) NOT NULL,
    allocationtype bigint NOT NULL,
    allocationdate timestamp with time zone NOT NULL,
    deallocationdate timestamp with time zone NOT NULL,
    allocatedobjectid bigint NOT NULL,
    e164numberid bigint NOT NULL
);

CREATE SEQUENCE seq_auditid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE audit (
    auditid bigint DEFAULT nextval('seq_auditid'::regclass) NOT NULL,
    username character varying(200),
    actiondate timestamp with time zone,
    tablename character varying(200),
    olddata text,
    newdata text
);

CREATE SEQUENCE seq_e164numberid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE e164number (
    e164numberid bigint DEFAULT nextval('seq_e164numberid'::regclass) NOT NULL,
    e164 character varying(50) NOT NULL,
    prefixid bigint NOT NULL,
    platformid bigint NOT NULL,
    lastallocationtime timestamp with time zone NOT NULL,
    allocatedobjectid bigint,
    allocationtype bigint,
    status integer NOT NULL,
    description character varying(500)
);

CREATE SEQUENCE seq_platformid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE platform (
    platformid bigint DEFAULT nextval('seq_platformid'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    ipgroup character varying(50) NOT NULL,
    defaultuserplatform boolean NOT NULL,
    allowrooms boolean,
    allowresources boolean,
    powershellenabled boolean
);

CREATE SEQUENCE seq_prefixid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE prefix (
    prefixid bigint DEFAULT nextval('seq_prefixid'::regclass) NOT NULL,
    prefix character varying(50) NOT NULL,
    siteid bigint NOT NULL,
    defaultplatformid bigint NOT NULL,
    allocationorder integer NOT NULL,
    allowallocation boolean,
    stateid bigint,
    preferredforrooms boolean,
    preferredforresources boolean
);

CREATE SEQUENCE seq_siteid
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE site (
    siteid bigint DEFAULT nextval('seq_siteid'::regclass) NOT NULL,
    name character varying(255) NOT NULL,
    adname character varying(255),
    stateid bigint,
    dialplan character varying
);

ALTER TABLE ONLY allocatedobject
    ADD CONSTRAINT allocatedobject_pkey PRIMARY KEY (allocatedobjectid);

ALTER TABLE ONLY allocationaudit
    ADD CONSTRAINT allocationaudit_pkey PRIMARY KEY (allocationauditid);

ALTER TABLE ONLY audit
    ADD CONSTRAINT audit_pkey PRIMARY KEY (auditid);

ALTER TABLE ONLY e164number
    ADD CONSTRAINT e164number_pkey PRIMARY KEY (e164numberid);

ALTER TABLE ONLY platform
    ADD CONSTRAINT platform_pkey PRIMARY KEY (platformid);

ALTER TABLE ONLY prefix
    ADD CONSTRAINT prefix_pkey PRIMARY KEY (prefixid);

ALTER TABLE ONLY site
    ADD CONSTRAINT site_pkey PRIMARY KEY (siteid);

ALTER TABLE ONLY allocatedobject
    ADD CONSTRAINT uq_allocobj_name UNIQUE (name);

ALTER TABLE ONLY e164number
    ADD CONSTRAINT uq_e164number_e164 UNIQUE (e164);

ALTER TABLE ONLY platform
    ADD CONSTRAINT uq_platform_name UNIQUE (name);

ALTER TABLE ONLY prefix
    ADD CONSTRAINT uq_prefix_prefix UNIQUE (prefix);

ALTER TABLE ONLY site
    ADD CONSTRAINT uq_site_name UNIQUE (name);

CREATE UNIQUE INDEX ix_single_user_platform ON platform USING btree (defaultuserplatform) WHERE (defaultuserplatform = true);

