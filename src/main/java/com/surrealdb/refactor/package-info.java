/**
 * @author surrealdb
 * The refactor package contains the new implementation of the driver.
 * The goal is to provide clear separation of the following concerns
 * - protocol (http(s), WebSocket plain text, WebSocket binary), Embedded JNI
 * - available functionality (realtime capabilities aren't available in stateless protocols like http)
 * - error handling
 * - forced authentication
 * - API compatibility with the Rust driver
 * - correct lifecycles
 * - correct types
 * - simple to navigate, test, and mock
 *
 * This change won't be a drop in replacement for the original driver, so treat this as experimental until
 * it does replace the original implementation.
 *
 * This interface is extremely likely to change. Primarily, I don't want to expose internal implementation details.
 * As I am writing this, I am aware this is happening for a lot of the code.
 * It will need refactoring to prevent exposing details.
 */
package com.surrealdb.refactor;
