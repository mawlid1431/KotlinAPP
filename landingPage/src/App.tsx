import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import { LoadingScreen } from '@/components/BrandLoader';

const LandingPage = lazy(() => import('@/landing/LandingPage').then((m) => ({ default: m.LandingPage })));
const NotFoundPage = lazy(() => import('@/components/NotFound').then((m) => ({ default: m.NotFoundPage })));

function Fallback() {
  return <LoadingScreen title="Loading…" />;
}

function Lazy({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<Fallback />}>{children}</Suspense>;
}

export default function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={
          <Lazy>
            <LandingPage />
          </Lazy>
        }
      />
      <Route path="*" element={<Lazy><NotFoundPage /></Lazy>} />
    </Routes>
  );
}
