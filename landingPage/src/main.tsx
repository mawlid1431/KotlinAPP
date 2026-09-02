import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';

let bootSplashDismissed = false;

function dismissBootSplash(): void {
  if (bootSplashDismissed) return;
  bootSplashDismissed = true;

  const boot = document.getElementById('ke-boot');
  if (!boot) return;

  boot.setAttribute('data-done', 'true');
  window.setTimeout(() => boot.remove(), 500);
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
);

requestAnimationFrame(() => requestAnimationFrame(dismissBootSplash));
window.setTimeout(dismissBootSplash, 600);
