WRANGLER_DIR := .wrangler
BIN_DIR := .wrangler/bin
SRC_DIRS := src test

.PHONY: test
test: build
	@ cd $(BIN_DIR) && node --test test/main_test.js

.PHONY: build
build:
	@ mkdir -p $(BIN_DIR)
	@ ly2k --target eval < build.clj > $(BIN_DIR)/Makefile
	@ $(MAKE) -f $(BIN_DIR)/Makefile > /dev/null
	@ cp $(LY2K_PACKAGES_DIR)/prelude/1.0.0/js/language_runtime.js $(BIN_DIR)/src/
	@ cp $(LY2K_PACKAGES_DIR)/prelude/1.0.0/js/language_runtime.js $(BIN_DIR)/test/

.PHONY: clean
clean:
	@ rm -rf $(BIN_DIR)

.PHONY: run
run: build
	@ cd $(WRANGLER_DIR) && wrangler dev --port 8787

.PHONY: deploy
deploy: test
	@ cd $(WRANGLER_DIR) && wrangler deploy
