type StorageType = "local" | "session";

class StorageUtils {
    private getStorage(type: StorageType): Storage | null {
        if (typeof window === "undefined") {
            return null;
        }

        return type === "local" ? window.localStorage : window.sessionStorage;
    }

    get<T>(key: string, type: StorageType = "local"): T | null {
        try {
            const storage = this.getStorage(type);
            const value = storage?.getItem(key);

            if (value == null) {
                return null;
            }

            return JSON.parse(value) as T;
        } catch (error) {
            console.error(error);
            return null;
        }
    }

    set<T>(key: string, value: T, type: StorageType = "local"): boolean {
        try {
            const storage = this.getStorage(type);

            if (!storage) {
                return false;
            }

            storage.setItem(key, JSON.stringify(value));
            return true;
        } catch (error) {
            console.error(error);
            return false;
        }
    }

    remove(key: string, type: StorageType = "local"): void {
        try {
            this.getStorage(type)?.removeItem(key);
        } catch (error) {
            console.error(error);
        }
    }

    clear(type: StorageType = "local"): void {
        try {
            this.getStorage(type)?.clear();
        } catch (error) {
            console.error(error);
        }
    }
}

const storageUtils = new StorageUtils();

export default storageUtils;
