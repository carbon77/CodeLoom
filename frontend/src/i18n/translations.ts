export const translations = {
  en: {
    'nav.problems': 'Problems', 'nav.admin': 'Admin', 'nav.profile': 'Profile',
    'language.label': 'Language', 'language.en': 'English', 'language.ru': 'Russian',
    'problems.title': 'Problems', 'problems.loadError': 'Unable to load problems. Please try again.',
    'problems.noMatch': 'No problems match your filters.', 'problems.empty': 'No problems available.',
    'problems.columns.title': 'Title', 'problems.columns.difficulty': 'Difficulty', 'problems.columns.published': 'Published',
    'filters.search': 'Search by title', 'filters.difficulty': 'Difficulty', 'filters.topic': 'Topic', 'filters.clear': 'Clear',
    'difficulty.EASY': 'Easy', 'difficulty.MEDIUM': 'Medium', 'difficulty.HARD': 'Hard',
    'profile.user': 'User', 'profile.email': 'Email:', 'profile.roles': 'Roles:',
    'profile.logout': 'Log out', 'profile.loggingOut': 'Signing out…', 'profile.logoutError': 'Logout failed. Please try again.',
    'errors.problemSlug': 'Problem slug is missing', 'errors.problemLoad': 'Unable to load problem.',
  },
  ru: {
    'nav.problems': 'Задачи', 'nav.admin': 'Администрирование', 'nav.profile': 'Профиль',
    'language.label': 'Язык', 'language.en': 'Английский', 'language.ru': 'Русский',
    'problems.title': 'Задачи', 'problems.loadError': 'Не удалось загрузить задачи. Попробуйте ещё раз.',
    'problems.noMatch': 'Нет задач, соответствующих фильтрам.', 'problems.empty': 'Нет доступных задач.',
    'problems.columns.title': 'Название', 'problems.columns.difficulty': 'Сложность', 'problems.columns.published': 'Опубликовано',
    'filters.search': 'Поиск по названию', 'filters.difficulty': 'Сложность', 'filters.topic': 'Тема', 'filters.clear': 'Сбросить',
    'difficulty.EASY': 'Лёгкая', 'difficulty.MEDIUM': 'Средняя', 'difficulty.HARD': 'Сложная',
    'profile.user': 'Пользователь', 'profile.email': 'Эл. почта:', 'profile.roles': 'Роли:',
    'profile.logout': 'Выйти', 'profile.loggingOut': 'Выход…', 'profile.logoutError': 'Не удалось выйти. Попробуйте ещё раз.',
    'errors.problemSlug': 'Не указан идентификатор задачи', 'errors.problemLoad': 'Не удалось загрузить задачу.',
  },
} as const

export type TranslationKey = keyof typeof translations.en
