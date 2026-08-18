# Wizard Spec Generator

The Spec Generator is the first half of the pipeline towards generating an Escape Room through a simple configuration file and schema.

## Install

- Clone the repo
- Run `npm install`

## Run

- `npm run dev` starts the Vite server
- Hit `o + Enter` in Vite or go to `http://localhost:5173/` by default

The direct Vite start is a browser-only development fallback. It stores draft
metadata in LocalStorage and uploaded bytes in IndexedDB; production
validation, finalization, and packaging require the local Java host. Draft
saves use the browser Web Locks API to serialize the LocalStorage CAS across
tabs. If Web Locks are unavailable, saving fails explicitly instead of writing
without cross-tab protection.
