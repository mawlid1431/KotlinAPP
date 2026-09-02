import { useEffect } from 'react';

/**
 * Sets document.title while a view is mounted and restores whatever was
 * there on unmount. Needed because this is a single-page app: without it
 * every route would keep the static title from index.html.
 */
export function useDocumentTitle(title: string): void {
  useEffect(() => {
    const previous = document.title;
    document.title = title;
    return () => {
      document.title = previous;
    };
  }, [title]);
}
