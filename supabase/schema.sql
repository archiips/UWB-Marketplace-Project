-- Dawgs Marketplace - Supabase Schema
-- Run this in the Supabase SQL Editor

-- Users table (populated on first Google Sign-In)
CREATE TABLE users (
    id TEXT PRIMARY KEY,           -- Google OAuth sub ID
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Listings table
CREATE TABLE listings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    price FLOAT NOT NULL CHECK (price >= 0),
    image_url TEXT,
    owner_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sold BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for search queries on title and description
CREATE INDEX idx_listings_title ON listings USING gin(to_tsvector('english', title));
CREATE INDEX idx_listings_description ON listings USING gin(to_tsvector('english', description));

-- Index for fetching a user's own listings quickly
CREATE INDEX idx_listings_owner ON listings(owner_id);
