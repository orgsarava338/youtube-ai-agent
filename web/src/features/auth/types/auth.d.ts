
interface CurrentUser {
  id: string;
  name: string;
  email: string;
  picture: string;
}

interface AuthContextValue {
  user: CurrentUser | null;
  loading: boolean;
  login: () => void;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

interface CsrfResponse {
    token: string;
    headerName: string;
}
