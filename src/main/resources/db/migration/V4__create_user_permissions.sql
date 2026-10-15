CREATE TABLE user_permissions (
                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                  public_id VARCHAR(36) NOT NULL,
                                  user_id BIGINT NOT NULL,
                                  permission_id BIGINT NOT NULL,

                                  is_active INT NOT NULL DEFAULT 1,
                                  is_deleted INT NOT NULL DEFAULT 0,

                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  created_by BIGINT NOT NULL,
                                  updated_at DATETIME NULL,
                                  updated_by BIGINT NULL,
                                  deleted_at DATETIME NULL,

                                  CONSTRAINT pk_user_permissions
                                      PRIMARY KEY (id),

                                  CONSTRAINT uk_user_permission
                                      UNIQUE (user_id, permission_id),

                                  CONSTRAINT uk_user_permissions_public_id
                                      UNIQUE (public_id),

                                  CONSTRAINT fk_user_permissions_user
                                      FOREIGN KEY (user_id) REFERENCES users(id),

                                  CONSTRAINT fk_user_permissions_permission
                                      FOREIGN KEY (permission_id) REFERENCES permissions(id),

                                  INDEX idx_user_permissions_user (user_id),
                                  INDEX idx_user_permissions_permission (permission_id)
);