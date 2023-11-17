SRC_DIR := src/main/java/lox
TGT_DIR := target/classes
SOURCE := $(wildcard $(SRC_DIR)/*.java)
TARGET := $(patsubst $(SRC_DIR)/%.java,$(TGT_DIR)/%.class,$(SOURCE))

all: $(TGT_DIR)/lox/Lox.class

$(TGT_DIR)/lox/Lox.class: $(SRC_DIR)/*.java | $(TGT_DIR)
	javac -d $(TGT_DIR) $(SOURCE)

$(TGT_DIR):
	mkdir -p $(TGT_DIR)

clean:
	rm -rf $(TGT_DIR)
