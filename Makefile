WRANGLER_DIR := .wrangler
BIN_DIR := .wrangler/bin
SRC_DIRS := src test

.PHONY: test
test: build
	@ cd $(WRANGLER_DIR) && node bin/test/main_test.js

.PHONY: build
build:
	@ mkdir -p $(BIN_DIR)
	@ ly2k compile -target eval -src build.clj > $(BIN_DIR)/Makefile
	@ $(MAKE) -f $(BIN_DIR)/Makefile > /dev/null

.PHONY: clean
clean:
	@ rm -rf $(BIN_DIR)

.PHONY: run
run: build
	@ cd $(WRANGLER_DIR) && wrangler dev --port 8787

.PHONY: deploy
deploy: test
	@ cd $(WRANGLER_DIR) && wrangler deploy
